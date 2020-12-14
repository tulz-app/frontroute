package io

import com.raquo.airstream.eventstream.EventStream
import io.frontroute.debug.Logging

package object frontroute {

  object directives extends Directives with PathMatchers

  def runRoute(route: Route, locationProvider: RouteLocationProvider): EventStream[() => Unit] = {
    var current = RoutingState.empty.path("!")
    locationProvider.stream
      .flatMap { location =>
        route(location, current.resetPath, RoutingState.empty).map {
          case RouteResult.Complete(next, action) =>
            if (next != current) {
              current = next
              Some(action)
            } else {
              Option.empty
            }
          case RouteResult.Rejected =>
            Logging.debug(s"route: rejected ($location)")
            Option.empty
        }
      }
      .collect { case Some(events) =>
        events
      }
      .flatten
  }

  type Directive0      = Directive[Unit]
  type Directive1[T]   = Directive[Tuple1[T]]
  type PathMatcher0    = PathMatcher[Unit]
  type PathMatcher1[T] = PathMatcher[Tuple1[T]]

  type Route = (RouteLocation, RoutingState, RoutingState) => EventStream[RouteResult]

  private[frontroute] def rejected: EventStream[RouteResult] = EventStream.fromValue(RouteResult.Rejected, emitOnce = true)

}
