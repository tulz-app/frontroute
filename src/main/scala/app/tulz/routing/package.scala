package app.tulz

import com.raquo.airstream.eventstream.EventStream
import org.scalajs.dom

import scala.language.implicitConversions

package object routing {

  object directives extends Directives with PathMatchers

  def runRoute(route: Route, locationProvider: RouteLocationProvider): EventStream[() => Unit] = {
    var current = RoutingState().path("!")
    locationProvider.stream
      .flatMap { location =>
        route(location, current.resetPath, RoutingState()).map {
          case RouteResult.Complete(next, action) =>
//            println(s"route result for $location:\n$next\n")
            if (next != current) {
              current = next
              Some(action)
            } else {
              Option.empty
            }
          case RouteResult.Rejected =>
            dom.console.debug(s"route: rejected ($location)")
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

  private[routing] def rejected: EventStream[RouteResult] = EventStream.fromValue(RouteResult.Rejected, emitOnce = true)

}
