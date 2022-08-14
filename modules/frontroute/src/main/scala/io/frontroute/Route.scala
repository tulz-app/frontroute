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
          val inserter = (child.maybe <-- runRoute(this, c.thisNode)).withContext(lockedContext)
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
    el: HtmlElement
  )(implicit owner: Owner): Signal[Option[Element]] = {
    var currentState                      = RoutingState.empty
    var currentSubscription: Subscription = null
    var currentLocationSubscription       = Option.empty[Subscription]
    val currentResult                     = Var(Option.empty[Element])

    val withState = el.ref.asInstanceOf[ElementWithLocationState]

    if (withState.____locationState.isEmpty) {
      withState.____locationState = new LocationState()
      DefaultLocationProvider.location.foreach(withState.____locationState.get.locationObserver.onNext)
    }
    val locationState = withState.____locationState.get

    val _ = {
      EventStream
        .merge(
          EventStream.fromValue(locationState.location.now()),
          locationState.location.changes
        )
        .collect { case Some(currentUnmatched) =>
          currentUnmatched
        }
        .flatMap { currentUnmatched =>
          route(
            currentUnmatched,
            currentState.resetPath,
            RoutingState.withPersistentData(currentState.persistent, currentState.async)
          )
        }
        .collect { case RouteResult.Complete(nextState, location, createResult) =>
          (nextState, location, createResult)
        }
        .foreach { case (nextState, location, createResult) =>
          locationState.emitRemaining(Some(location))
          if (nextState != currentState) {
            currentState = nextState
            val created = createResult()

            if (currentSubscription != null) {
              currentSubscription.kill()
              currentSubscription = null
            }
            currentSubscription = created.foreach { result =>
              currentResult.now().foreach { previous =>
                currentLocationSubscription.foreach(_.kill())
                currentLocationSubscription = None
                previous.ref.asInstanceOf[ElementWithLocationState].____locationState = null
              }
              val resultWithState = result.ref.asInstanceOf[ElementWithLocationState]
              val resultState     = new LocationState()
              resultWithState.____locationState = resultState

              val sub = locationState.subscribeToRemaining(resultState.locationObserver)
              currentLocationSubscription = Some(sub)
              currentResult.set(Option(result))
            }
          }
        }
    }
    currentResult.signal
  }

}
