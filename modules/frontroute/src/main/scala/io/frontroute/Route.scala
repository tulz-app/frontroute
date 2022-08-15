package io.frontroute

import com.raquo.laminar.api.L._
import com.raquo.laminar.lifecycle.InsertContext
import io.frontroute.internal.ElementWithLocationState
import io.frontroute.internal.LocationState
import io.frontroute.internal.RoutingState

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
    var currentState                      = RoutingState.empty
    var currentSubscription: Subscription = null
    val currentResult                     = Var(Option.empty[Element])

    val withState = ctx.thisNode.ref.asInstanceOf[ElementWithLocationState]

    if (withState.____locationState.isEmpty) {
      val siblingMatched   = Var(false)
      val onSiblingMatched = siblingMatched.writer.contramap { (_: Unit) => true }
      DefaultLocationProvider.location.foreach { _ => siblingMatched.set(false) }

      withState.____locationState = new LocationState(DefaultLocationProvider.location, siblingMatched.signal, onSiblingMatched, owner)
    }
    val locationState = withState.____locationState.get

    def killPrevious(): Unit = {
      if (currentSubscription != null) {
        currentSubscription.kill()
        currentSubscription = null
      }
      currentResult.now().foreach { previous =>
        previous.ref.asInstanceOf[ElementWithLocationState].____locationState.foreach(_.kill())
        previous.ref.asInstanceOf[ElementWithLocationState].____locationState = null
      }
    }

    val _ = {

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
            currentState.resetPath,
            RoutingState.withPersistentData(currentState.persistent, currentState.async)
          )
        }
        .foreach {
          case RouteResult.Complete(nextState, location, createResult) =>
            locationState.emitRemaining(Some(location))
            locationState.notifyMatched()
            if (nextState != currentState) {
              currentState = nextState
              val created = createResult()

              killPrevious()
              currentSubscription = created.foreach { result =>
                val resultWithState = result.ref.asInstanceOf[ElementWithLocationState]
                val resultState     = new LocationState(locationState.remaining, locationState.$childMatched, locationState.onChildMatched, owner)
                resultWithState.____locationState = resultState

                currentResult.set(Option(result))
              }
            }

          case RouteResult.Rejected =>
            killPrevious()
            currentResult.set(None)
        }
    }

    currentResult.signal
  }

}
