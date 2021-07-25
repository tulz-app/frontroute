package io.frontroute

import com.raquo.airstream.core.EventStream
import io.frontroute.internal.RoutingState

object Types {

  type Directive0   = Directive[Unit]
  type PathMatcher0 = PathMatcher[Unit]

  type Route = (RouteLocation, RoutingState, RoutingState) => EventStream[RouteResult]

}
