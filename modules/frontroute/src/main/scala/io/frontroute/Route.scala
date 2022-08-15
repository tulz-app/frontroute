package io.frontroute

import com.raquo.laminar.api.L._
import com.raquo.laminar.lifecycle.InsertContext
import io.frontroute.internal.ElementWithLocationState
import io.frontroute.internal.LocationState
import io.frontroute.internal.RoutingState
import org.scalajs.dom.html

import scala.annotation.tailrec

trait Route extends ((RouteLocation, RoutingState, RoutingState) => EventStream[RouteResult]) with Mod[HtmlElement] {

  override def apply(element: HtmlElement): Unit = {
    var maybeSubscription: Option[DynamicSubscription] = None
    val lockedContext                                  = InsertContext.reserveSpotContext[HtmlElement](element)

    element.amend(
      onMountUnmountCallback[HtmlElement](
        mount = { c =>
          import c.owner
          val inserter = (child.maybe <-- runRoute(this, c)).withContext(lockedContext)
          maybeSubscription = Some(inserter.bind(c.thisNode))
        },
        unmount = { _ =>
          maybeSubscription.foreach(_.kill())
          maybeSubscription = None
        }
      )
    )
  }

  private def runRoute(
    route: Route,
    ctx: MountContext[HtmlElement]
  )(implicit owner: Owner): Signal[Option[Element]] = {
    var currentSubscription: Subscription = null
    val currentResult                     = Var(Option.empty[Element])
    val locationState                     = getClosestLocationState(ctx.thisNode.ref)

    def killPrevious(): Unit = {
      if (currentSubscription != null) {
        currentSubscription.kill()
        currentSubscription = null
      }
      currentResult.now().foreach { previous =>
        previous.ref.asInstanceOf[ElementWithLocationState].____locationState.foreach(_.kill())
      }
    }

    val _ =
      EventStream
        .merge(
          EventStream.fromValue(locationState.location.now()).delay(0),
          locationState.location.changes.delay(0)
        )
        .collect { case Some(currentUnmatched) =>
          currentUnmatched
        }
        .flatMap { currentUnmatched =>
          route(
            currentUnmatched.copy(otherMatched = locationState.siblingMatched),
            locationState.currentState.resetPath,
            RoutingState.withPersistentData(locationState.currentState.persistent, locationState.currentState.async)
          )
        }
        .flatMap {
          case RouteResult.Complete(nextState, location, createResult) =>
            if (nextState != locationState.currentState) {
              createResult().map { result =>
                Some((nextState, location, Some(result)))
              }
            } else {
              EventStream.fromValue(Some((nextState, location, None)))
            }

          case RouteResult.Rejected =>
            EventStream.fromValue(None)

        }
        .foreach {
          case RouteResult.Complete(nextState, location, createResult) =>
            if (nextState != locationState.currentState) {
              locationState.currentState = nextState

              val created = createResult()

              killPrevious()
              currentSubscription = created.foreach { result =>
                val resultWithState = result.ref.asInstanceOf[ElementWithLocationState]
                if (resultWithState.____locationState.isEmpty) {
                  resultWithState.____locationState = new LocationState(locationState.remaining, locationState.$childMatched, locationState.onChildMatched, owner)
                }
                resultWithState.____locationState.get.start()

                currentResult.set(Option(result))
              }
            }
            locationState.emitRemaining(Some(location))
            locationState.notifyMatched()

          case RouteResult.Rejected =>
            killPrevious()
            locationState.currentState = RoutingState.empty
            currentResult.set(None)
        }

    currentResult.signal
  }

  @tailrec
  private def getClosestLocationState(
    node: html.Element
  )(implicit owner: Owner): LocationState = {
    val withState = node.asInstanceOf[ElementWithLocationState]
    if (withState.____locationState.isEmpty) {
      if (node.parentElement != null) {
        getClosestLocationState(node.parentElement)
      } else {
        val siblingMatched   = Var(false)
        val onSiblingMatched = siblingMatched.writer.contramap { (_: Unit) => true }
        DefaultLocationProvider.location.foreach { _ => siblingMatched.set(false) }
        val locationState    = new LocationState(DefaultLocationProvider.location, siblingMatched.signal, onSiblingMatched, owner)
        withState.____locationState = locationState
        locationState.start()
        locationState
      }
    } else {
      withState.____locationState.get
    }
  }

}
