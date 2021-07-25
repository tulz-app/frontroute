package io.frontroute

import com.raquo.airstream.ownership.Owner
import com.raquo.airstream.ownership.Subscription
import io.frontroute.internal.RoutingState

trait RunRoute {

  def runRoute(route: Route, locationProvider: LocationProvider)(implicit owner: Owner): Subscription = {
    var current = RoutingState.empty
    locationProvider.stream
      .flatMap { location =>
        route(location, current.resetPath, RoutingState.withPersistentData(current.persistent)).map {
          case RouteResult.Complete(next, action) =>
            if (next != current) {
              current = next
              Some(action)
            } else {
              Option.empty
            }
          case RouteResult.Rejected =>
            Option.empty
        }
      }
      .collect { case Some(events) =>
        events
      }
      .flatten
      .foreach(_())
  }

}
