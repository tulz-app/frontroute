package io.frontroute

import com.raquo.airstream.core.EventStream
import io.frontroute.internal.RoutingState

object Types {

  type PathMatcher0 = PathMatcher[Unit]

  type TypedRoute[A] = (RouteLocation, RoutingState, RoutingState) => EventStream[RouteResult[A]]

}
