package io.frontroute

import com.raquo.laminar.api.L._
import io.frontroute.internal.RoutingState

trait RunRoute[A] {
  self: RouteDSL[A] =>

  def runRoute(
    route: Route
  )(implicit owner: Owner, locationProvider: LocationProvider = LocationProvider.defaultProvider): Signal[Option[A]] = {

    var currentState                      = RoutingState.empty
    var currentSubscription: Subscription = null
    val currentResult                     = Var(Option.empty[A])

    locationProvider.stream
      .flatMap { location =>
        route(
          location,
          currentState.resetPath,
          RoutingState.withPersistentData(currentState.persistent, currentState.async)
        ).map {
          case RouteResult.Complete(nextState, createResult) =>
            if (nextState != currentState) {
              currentState = nextState
              Some(createResult)
            } else {
              Option.empty
            }
          case RouteResult.Rejected                          =>
            Option.empty
        }
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
