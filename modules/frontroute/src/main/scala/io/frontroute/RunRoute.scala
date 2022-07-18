package io.frontroute

import com.raquo.laminar.api.L._
import io.frontroute.internal.RoutingState

trait RunRoute {

  def runRoute(
    route: Route
  )(implicit owner: Owner): Signal[Option[Element]] = {

    var currentState                      = RoutingState.empty
    var currentSubscription: Subscription = null
    val currentResult                     = Var(Option.empty[Element])

    GlobalState.locationChanges
      .mapTo {
        GlobalState.currentUnmatched
      }
      .flatMap {
        case Some(location) =>
          route(
            location,
            currentState.resetPath,
            RoutingState.withPersistentData(currentState.persistent, currentState.async)
          ).map {
            case RouteResult.Complete(nextState, nextLocation, createResult) =>
              if (nextState != currentState) {
                currentState = nextState
                GlobalState.setCurrentUnmatched(nextLocation)
                Some(createResult)
              } else {
                Option.empty
              }
            case RouteResult.Rejected                                        =>
              Option.empty
          }
        case None           => EventStream.empty
      }
      .collect { case Some(createView) => createView() }
      .foreach { createResult =>
        if (currentSubscription != null) {
          currentSubscription.kill()
          currentSubscription = null
        }
        currentSubscription = createResult.foreach { result =>
          currentResult.set(Option(result))
        }
      }

    currentResult.signal
  }

}
