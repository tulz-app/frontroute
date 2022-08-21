package io

import com.raquo.laminar.api.L._
import app.tulz.tuplez.ApplyConverter
import app.tulz.tuplez.ApplyConverters
import com.raquo.airstream.core.Signal
import com.raquo.laminar.nodes.ReactiveHtmlElement
import io.frontroute.internal.UrlString
import io.frontroute.ops.DirectiveOfOptionOps
import org.scalajs.dom
import org.scalajs.dom.HTMLAnchorElement

package object frontroute extends PathMatchers with Directives with ApplyConverters[Route] {

  type PathMatcher0 = PathMatcher[Unit]

  type Directive0 = Directive[Unit]

  implicit def directiveOfOptionSyntax(underlying: Directive[Option[Element]]): DirectiveOfOptionOps = new DirectiveOfOptionOps(underlying)

  private[frontroute] val rejected: Signal[RouteResult] = Val(RouteResult.Rejected)

  val reject: Route = (_, _, _) => rejected

  def debug(message: Any, optionalParams: Any*)(subRoute: Route): Route = { (location, previous, state) =>
    dom.console.debug(message, optionalParams: _*)
    subRoute(location, previous, state)
  }

  def concat(routes: Route*): Route = (location, previous, state) => {

    def findFirst(rs: List[(Route, Int)]): Signal[RouteResult] =
      rs match {
        case Nil                    => rejected
        case (route, index) :: tail =>
          route(location, previous, state.enterConcat(index)).flatMap {
            case RouteResult.Matched(state, location, result) => Val(RouteResult.Matched(state, location, result))
            case RouteResult.Rejected                         => findFirst(tail)
          }
      }

    findFirst(routes.zipWithIndex.toList)
  }

  implicit def addDirectiveApply[L](directive: Directive[L])(implicit hac: ApplyConverter[L, Route]): hac.In => Route = { subRoute => (ctx, previous, state) =>
    val result = directive.tapply(hac(subRoute))(ctx, previous, state)
    result
  }

  implicit def addNullaryDirectiveApply(directive: Directive0): Route => Route = { subRoute => (ctx, previous, state) =>
    directive.tapply(_ => subRoute)(ctx, previous, state)
  }

  private def complete(result: => ToComplete): Route = (location, _, state) => Val(RouteResult.Matched(state, location, () => result.get))

  implicit def elementToRoute(e: => Element): Route = complete(e)

  implicit def signalOfElementToRoute(e: => Signal[Element]): Route = complete(e)

  def navMod(mod: Signal[Boolean] => Mod[ReactiveHtmlElement[HTMLAnchorElement]]): Mod[ReactiveHtmlElement[HTMLAnchorElement]] =
    inContext { el =>
      val UrlString(url) = el.ref.href
      val active         =
        DefaultLocationProvider.location.map {
          case None    => false
          case Some(l) => l.fullPath.mkString("/", "/", "").startsWith(url.pathname)
        }
      mod(active)
    }

}
