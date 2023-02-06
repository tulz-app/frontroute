package io

import com.raquo.laminar.api.L._
import app.tulz.tuplez.ApplyConverter
import app.tulz.tuplez.ApplyConverters
import com.raquo.airstream.core.Signal
import com.raquo.laminar.nodes.ReactiveElement
import com.raquo.laminar.nodes.ReactiveHtmlElement
import io.frontroute.internal.LocationState
import io.frontroute.internal.UrlString
import io.frontroute.ops.DirectiveOfOptionOps
import org.scalajs.dom
import org.scalajs.dom.HTMLAnchorElement
import org.scalajs.dom.html

import scala.annotation.tailrec
import scala.scalajs.js

package object frontroute extends PathMatchers with Directives with ApplyConverters[Route] {

  type PathMatcher0 = PathMatcher[Unit]

  type Directive0 = Directive[Unit]

  def locationProvider(lp: LocationProvider): Modifier[Element] =
    onMountCallback { ctx =>
      val currentState = LocationState(ctx.thisNode)
      if (currentState.isDefined) {
        throw new IllegalStateException("initializing location provider: location state is already defined")
      }
      val _            = LocationState.initIfMissing(
        ctx.thisNode.ref,
        () => LocationState.withLocationProvider(lp)(ctx.owner)
      )
    }

  implicit def directiveOfOptionSyntax[L](underlying: Directive[Option[L]]): DirectiveOfOptionOps[L] = new DirectiveOfOptionOps(underlying)

  private[frontroute] val rejected: RouteResult = RouteResult.Rejected

  val reject: Route = (_, _, _) => rejected

  def debug(message: Any, optionalParams: Any*)(subRoute: Route): Route = { (location, previous, state) =>
    dom.console.debug(message, optionalParams: _*)
    subRoute(location, previous, state)
  }

  @deprecated("use firstMatch instead", "0.16.0")
  def concat(routes: Route*): Route = firstMatch(routes: _*)

  def firstMatch(routes: Route*): Route = (location, previous, state) => {

    @tailrec
    def findFirst(rs: List[(Route, Int)]): RouteResult =
      rs match {
        case Nil                    => rejected
        case (route, index) :: tail =>
          route(location, previous, state.enterConcat(index)) match {
            case RouteResult.Matched(state, location, consumed, result) => RouteResult.Matched(state, location, consumed, result)
            case RouteResult.RunEffect(state, location, consumed, run)  => RouteResult.RunEffect(state, location, consumed, run)
            case RouteResult.Rejected                                   => findFirst(tail)
          }
      }

    findFirst(routes.zipWithIndex.toList)
  }

  implicit def addDirectiveApply[L](directive: Directive[L])(implicit hac: ApplyConverter[L, Route]): hac.In => Route = { subRoute => (location, previous, state) =>
    val result = directive.tapply(hac(subRoute))(location, previous, state)
    result
  }

  implicit def addNullaryDirectiveApply(directive: Directive0): Route => Route = { subRoute => (location, previous, state) =>
    directive.tapply(_ => subRoute)(location, previous, state)
  }

  private def complete(result: () => HtmlElement): Route = (location, _, state) => RouteResult.Matched(state, location, state.consumed, result)

  def runEffect(effect: => Unit): Route = (location, _, state) =>
    RouteResult.RunEffect(
      state,
      location,
      List.empty,
      () => effect
    )

  def navigate(
    to: String,
    replace: Boolean = false,
  ): Route = runEffect {
    if (replace) {
      BrowserNavigation.replaceState(url = to)
    } else {
      BrowserNavigation.pushState(url = to)
    }
  }

  implicit def elementToRoute(e: => HtmlElement): Route = complete(() => e)

  private[frontroute] def withMatchedPathAndEl[Ref <: dom.html.Element](
    mod: (ReactiveHtmlElement[Ref], StrictSignal[List[String]]) => Mod[ReactiveHtmlElement[Ref]]
  ): Mod[ReactiveHtmlElement[Ref]] = {
    inContext { el =>
      val consumedVar                          = Var(List.empty[String])
      var sub: js.UndefOr[DynamicSubscription] = js.undefined

      LocationState.closest(el.ref) match {
        case None                =>
          sub = ReactiveElement.bindFn(el, LocationState.default.consumed) { next =>
            LocationState.closest(el.ref) match {
              case None                => consumedVar.set(next)
              case Some(locationState) =>
                sub.foreach(_.kill())
                sub = js.undefined
                // managed subscription
                val _ = ReactiveElement.bindObserver(el, locationState.consumed)(consumedVar.writer)
            }
          }
        case Some(locationState) =>
          // managed subscription
          val _ = ReactiveElement.bindObserver(el, locationState.consumed)(consumedVar.writer)
      }
      mod(el, consumedVar.signal)
    }
  }

  def withMatchedPath[Ref <: dom.html.Element](mod: StrictSignal[List[String]] => Mod[ReactiveHtmlElement[Ref]]): Mod[ReactiveHtmlElement[Ref]] = {
    withMatchedPathAndEl((_, consumed) => mod(consumed))
  }

  def relativeHref(path: String): Mod[ReactiveHtmlElement[html.Anchor]] =
    withMatchedPath { matched =>
      href <-- matched.map(_.mkString("/", "/", s"/$path"))
    }

  def navMod(
    mod: Signal[Boolean] => Mod[ReactiveHtmlElement[HTMLAnchorElement]]
  ): Mod[ReactiveHtmlElement[HTMLAnchorElement]] =
    inContext { el =>
      val active = LocationState.closestOrDefault(el.ref).location.map { location =>
        val UrlString(url) = el.ref.href
        location.exists { location =>
          location.fullPath.mkString("/", "/", "").startsWith(url.pathname)
        }
      }
      mod(active)
    }

  def navModExact(
    mod: Signal[Boolean] => Mod[ReactiveHtmlElement[HTMLAnchorElement]]
  ): Mod[ReactiveHtmlElement[HTMLAnchorElement]] =
    inContext { el =>
      val active = LocationState.closestOrDefault(el.ref).location.map { location =>
        val UrlString(url) = el.ref.href
        location.exists { location =>
          location.fullPath.mkString("/", "/", "") == url.pathname
        }
      }
      mod(active)
    }

  def matchedMod(
    mod: Signal[Boolean] => Mod[ReactiveHtmlElement[HTMLAnchorElement]]
  ): Mod[ReactiveHtmlElement[HTMLAnchorElement]] =
    withMatchedPathAndEl { (el, matched) =>
      val active =
        matched.map { l =>
          val UrlString(url) = el.ref.href
          println(s"nav mod url.pathname: ${url.pathname}")
          println(s"nav mod matched: $l")
          l.mkString("/", "/", "").startsWith(url.pathname)
        }
      mod(active)
    }

  def matchedModExact(
    mod: Signal[Boolean] => Mod[ReactiveHtmlElement[HTMLAnchorElement]]
  ): Mod[ReactiveHtmlElement[HTMLAnchorElement]] =
    withMatchedPathAndEl { (el, consumed) =>
      val active =
        consumed.map { l =>
          val UrlString(url) = el.ref.href
          l.mkString("/", "/", "") == url.pathname
        }
      mod(active)
    }

}
