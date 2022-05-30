package io.frontroute

import com.raquo.laminar.api.L._
import io.frontroute.internal.RoutingState

object Types {

  type Route = (RouteLocation, RoutingState, RoutingState) => EventStream[RouteResult]

}
