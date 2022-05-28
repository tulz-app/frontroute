package io.frontroute

import com.raquo.laminar.api.L._
import io.frontroute.internal.RoutingState

sealed trait RouteResult[+A] extends Product with Serializable

object RouteResult {
  final case class Complete[A](state: RoutingState, result: () => Signal[A]) extends RouteResult[A]
  case object Rejected                                                       extends RouteResult[Nothing]
}
