package io.frontroute

import com.raquo.airstream.eventstream.EventStream

sealed trait RouteResult extends Product with Serializable

object RouteResult {
  final case class Complete(state: RoutingState, action: EventStream[() => Unit]) extends RouteResult
  case object Rejected                                                            extends RouteResult
}
