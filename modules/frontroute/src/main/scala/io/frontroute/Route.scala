package io.frontroute

import com.raquo.laminar.api.L._
import com.raquo.laminar.lifecycle.InsertContext
import io.frontroute.internal.ElementWithLocationState
import io.frontroute.internal.LocationState
import io.frontroute.internal.RoutingState
import io.frontroute.internal.RoutingStateRef
import io.frontroute.internal.SignalToStream
import org.scalajs.dom.html

import scala.annotation.tailrec

trait Route extends ((RouteLocation, RoutingState, RoutingState) => Signal[RouteResult]) with Mod[HtmlElement] {

  import Route._

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
    var latestRender  = Option.empty[Element]
    val currentRender = Var(Option.empty[Element])
    val locationState = getClosestLocationState(ctx.thisNode.ref)
    val childStateRef = new RoutingStateRef

    def killPrevious(): Unit = {
      currentRender.now().foreach { previous =>
        previous.ref.asInstanceOf[ElementWithLocationState].____locationState.foreach(_.kill())
      }
    }

    val _ =
      SignalToStream(locationState.location)
        .delay(0)
        .collect { case Some(currentUnmatched) =>
          currentUnmatched
        }
        .flatMap { currentUnmatched =>
          val routingState = locationState.currentState.get(this).getOrElse(RoutingState.empty)
          SignalToStream(
            route(
              currentUnmatched.copy(otherMatched = locationState.siblingMatched),
              routingState.resetPath,
              RoutingState.withPersistentData(routingState.persistent, routingState.async)
            )
          )
        }
        .flatMap {
          case RouteResult.Complete(nextState, location, createResult) =>
            val routingState = locationState.currentState.get(this)
            if (!routingState.contains(nextState)) {
              SignalToStream(
                createResult().map { result =>
                  RouteEvent.NextRender(nextState, location, result)
                }
              )
            } else {
              EventStream.fromValue(
                RouteEvent.SameRender(nextState, location)
              )
            }

          case RouteResult.Rejected =>
            EventStream.fromValue(
              RouteEvent.NoRender
            )
        }
        .foreach {
          case RouteEvent.NextRender(nextState, remaining, render) =>
            killPrevious()
            val resultWithState = render.ref.asInstanceOf[ElementWithLocationState]
            if (resultWithState.____locationState.isEmpty) {
              resultWithState.____locationState = new LocationState(
                locationState.remaining,
                locationState.$childMatched,
                locationState.onChildMatched,
                childStateRef,
                owner
              )
            }
            resultWithState.____locationState.get.start()

            locationState.currentState.set(this, nextState)

            locationState.emitRemaining(Some(remaining))
            locationState.notifyMatched()

            latestRender = Some(render)
            currentRender.set(Some(render))

          case RouteEvent.SameRender(nextState, remaining) =>
            locationState.currentState.set(this, nextState)

            locationState.emitRemaining(Some(remaining))
            locationState.notifyMatched()
            currentRender.set(latestRender)

          case RouteEvent.NoRender =>
            killPrevious()
            locationState.currentState.unset(this)
            latestRender = None
            currentRender.set(None)
        }

    currentRender.signal
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
        val locationState    = new LocationState(
          DefaultLocationProvider.location,
          siblingMatched.signal,
          onSiblingMatched,
          new RoutingStateRef,
          owner
        )
        withState.____locationState = locationState
        locationState.start()
        locationState
      }
    } else {
      withState.____locationState.get
    }
  }

}

object Route {

  sealed private[frontroute] trait RouteEvent extends Product with Serializable

  private[frontroute] object RouteEvent {

    case class NextRender(
      nextState: RoutingState,
      remaining: RouteLocation,
      render: Element
    ) extends RouteEvent

    case class SameRender(
      nextState: RoutingState,
      remaining: RouteLocation
    ) extends RouteEvent

    case object NoRender extends RouteEvent

  }

}
