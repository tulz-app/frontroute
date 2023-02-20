package frontroute

import com.raquo.laminar.api.L._
import frontroute.internal.RoutingState

sealed trait RouteResult extends Product with Serializable

object RouteResult {
  final case class Matched(state: RoutingState, location: Location, consumed: List[String], result: () => HtmlElement) extends RouteResult
  final case class RunEffect(state: RoutingState, location: Location, consumed: List[String], run: () => Unit)         extends RouteResult
  case object Rejected                                                                                                 extends RouteResult
}
