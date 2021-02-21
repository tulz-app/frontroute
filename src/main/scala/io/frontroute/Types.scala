package io.frontroute

import com.raquo.airstream.core.EventStream

object Types {

  type Directive0   = Directive[Unit]
  type PathMatcher0 = PathMatcher[Unit]

  type Route = (RouteLocation, RoutingState, RoutingState) => EventStream[RouteResult]

}
