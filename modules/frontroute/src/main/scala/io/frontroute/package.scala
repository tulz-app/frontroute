package io

import com.raquo.laminar.api.L._
import app.tulz.tuplez.ApplyConverter
import app.tulz.tuplez.ApplyConverters
import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Signal
import io.frontroute.ops.DirectiveOfOptionOps
import org.scalajs.dom

package object frontroute extends PathMatchers with Directives with ApplyConverters[Route] {

  type PathMatcher0 = PathMatcher[Unit]

  type Directive0 = Directive[Unit]

  implicit def directiveOfOptionSyntax(underlying: Directive[Option[Element]]): DirectiveOfOptionOps = new DirectiveOfOptionOps(underlying)

  def reject: Route = (_, _, _) => rejected

  def complete(result: => ToComplete[Element]): Route = (location, _, state) =>
    EventStream.fromValue(
      RouteResult.Complete(state, location, () => result.get)
    )

  def debug(message: Any, optionalParams: Any*)(subRoute: Route): Route =
    (location, previous, state) => {
      dom.console.debug(message, optionalParams: _*)
      subRoute(location, previous, state)
    }

  def concat(routes: Route*): Route = (location, previous, state) => {
    def findFirst(rs: List[(Route, Int)]): EventStream[RouteResult] =
      rs match {
        case Nil                    => rejected
        case (route, index) :: tail =>
          route(location, previous, state.enterConcat(index)).flatMap {
            case RouteResult.Complete(state, location, result) => EventStream.fromValue(RouteResult.Complete(state, location, result))
            case RouteResult.Rejected                          => findFirst(tail)
          }
      }

    findFirst(routes.zipWithIndex.toList)
  }

  implicit def toDirective[L](route: Route): Directive[L] = Directive[L](_ => route)

  implicit def addDirectiveApply[L](directive: Directive[L])(implicit hac: ApplyConverter[L, Route]): hac.In => Route =
    subRoute =>
      (ctx, previous, state) => {
        val result = directive.tapply(hac(subRoute))(ctx, previous, state)
        result
      }

  implicit def addNullaryDirectiveApply(directive: Directive0): Route => Route =
    subRoute =>
      (ctx, previous, state) => {
        val result = directive.tapply(_ => subRoute)(ctx, previous, state)
        result
      }

  implicit def liftElementIntoVal(element: Element): Signal[Element] = Val(element)

  implicit def elementToRoute(e: => Element): Route = complete(e)

  implicit def signalOfElementToRoute(e: => Signal[Element]): Route = complete(e)

  private[frontroute] def rejected: EventStream[RouteResult] = EventStream.fromValue(RouteResult.Rejected)

  def routeLink(href: String, mods: (Signal[Boolean] => Mod[HtmlElement])*): Element = {
    val active = DefaultLocationProvider.isActive(href)
    a(
      com.raquo.laminar.api.L.href := href,
      mods.map(_(active))
    )
  }

}
