import com.raquo.laminar.api.L._

import app.tulz.tuplez.ApplyConverter
import app.tulz.tuplez.ApplyConverters
import com.raquo.airstream.core.Signal
import com.raquo.laminar.nodes.ReactiveElement
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontroute.internal.LocationState
import frontroute.internal.UrlString
import frontroute.ops.DirectiveOfOptionOps
import org.scalajs.dom
import org.scalajs.dom.HTMLAnchorElement
import org.scalajs.dom.HTMLDivElement
import org.scalajs.dom.MutationObserver
import org.scalajs.dom.MutationObserverInit
import org.scalajs.dom.MutationRecord
import org.scalajs.dom.html

import scala.annotation.tailrec
import scala.scalajs.js

package object frontroute extends PathMatchers with Directives with ApplyConverters[Route] {

  type PathMatcher0 = PathMatcher[Unit]

  type Directive0 = Directive[Unit]

  implicit def directiveOfOptionSyntax[L](underlying: Directive[Option[L]]): DirectiveOfOptionOps[L] = new DirectiveOfOptionOps(underlying)

  private[frontroute] val rejected: RouteResult = RouteResult.Rejected

  val reject: Route = (_, _, _) => rejected

  def debug(message: Any, optionalParams: Any*)(subRoute: Route): Route = { (location, previous, state) =>
    dom.console.debug(message, optionalParams: _*)
    subRoute(location, previous, state)
  }

  @deprecated("use firstMatch instead", "0.16.0")
  def concat(routes: Route*): Route = firstMatch(routes: _*)

  def initRouting: Modifier[Element] = {
    initRouting(LocationProvider.windowLocationProvider)
  }

  def initRouting(lp: LocationProvider): Modifier[Element] =
    onMountCallback { ctx =>
      LocationState.init(
        ctx.thisNode.ref,
        LocationState.withLocationProvider(lp)(ctx.owner)
      )
    }

  def routes[M](mods: Modifier[Element]*): ReactiveHtmlElement[HTMLDivElement] =
    div(
      styleAttr := "display: contents",
      initRouting,
      mods
    )

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
    directive.tapply(hac(subRoute))(location, previous, state)
  }

//  implicit def addDirectiveExecute[L](directive: Directive[L])(implicit hac: ApplyConverter[L, Unit]): DirectiveExecute[hac.In] = new DirectiveExecute[hac.In] {
//    def execute(run: hac.In): Route = {
//      directive.tapply { l =>
//        runEffect {
//          hac(run)(l)
//        }
//      }
//    }
//  }

  implicit def addDirectiveExecute[L](directive: Directive[L]): DirectiveExecute[L => Unit] = new DirectiveExecute[L => Unit] {
    def execute(run: L => Unit): Route = {
      directive.tapply { l =>
        runEffect {
          run(l)
        }
      }
    }
  }

  implicit def addNullaryDirectiveApply(directive: Directive0): Route => Route = { subRoute => (location, previous, state) =>
    directive.tapply(_ => subRoute)(location, previous, state)
  }

  implicit def addNullaryDirectiveExecute(directive: Directive0): DirectiveUnitExecute = new DirectiveUnitExecute {
    def execute(run: => Unit): Route = {
      directive.tapply { _ =>
        runEffect {
          run
        }
      }
    }
  }

  private def complete(result: () => HtmlElement): Route = (location, _, state) => RouteResult.Matched(state, location, state.consumed, result)

  def runEffect(effect: => Unit): Route = (location, _, state) =>
    RouteResult.RunEffect(
      state,
      location,
      List.empty,
      () => effect
    )

  private def makeRelative(matched: List[String], path: String): String =
    if (matched.nonEmpty) {
      if (path.nonEmpty) {
        matched.mkString("/", "/", s"/$path")
      } else {
        matched.mkString("/", "/", "")
      }
    } else {
      if (path.nonEmpty) {
        s"/$path"
      } else {
        "/"
      }
    }

  def navigate(
    to: String,
    replace: Boolean = false,
  ): Route =
    extractMatchedPath { matched =>
      runEffect {
        if (replace) {
          BrowserNavigation.replaceState(url = makeRelative(matched, to))
        } else {
          BrowserNavigation.pushState(url = makeRelative(matched, to))
        }
      }
    }

  implicit def elementToRoute(e: => HtmlElement): Route = complete(() => e)

  def withMatchedPath[Ref <: dom.html.Element](mod: StrictSignal[List[String]] => Mod[ReactiveHtmlElement[Ref]]): Mod[ReactiveHtmlElement[Ref]] = {
    val consumedVar = Var(List.empty[String])
    Seq(
      onMountCallback { (ctx: MountContext[ReactiveHtmlElement[Ref]]) =>
        val locationState = LocationState.closestOrFail(ctx.thisNode.ref)
        val consumed      =
          EventStream.fromValue(()).delay(0).flatMap { _ =>
            locationState.consumed
          }

        val _ = ReactiveElement.bindObserver(ctx.thisNode, consumed)(consumedVar.writer)
      },
      mod(consumedVar.signal)
    )
  }

  def relativeHref(path: String): Mod[ReactiveHtmlElement[html.Anchor]] =
    withMatchedPath { matched =>
      href <-- matched.map { matched =>
        makeRelative(matched, path)
      }
    }

  def navModFn(compare: (Location, org.scalajs.dom.Location) => Boolean)(
    mod: Signal[Boolean] => Mod[ReactiveHtmlElement[HTMLAnchorElement]]
  ): Mod[ReactiveHtmlElement[HTMLAnchorElement]] = {
    val activeVar = Var(false)
    val mutations = EventBus[Seq[MutationRecord]]()

    val mutationObserver = new MutationObserver(
      callback = (entries, _) => {
        if (entries.nonEmpty) {
          mutations.emit(entries.toSeq)
        }
      }
    )

    Seq(
      onMountUnmountCallback(
        mount = { (ctx: MountContext[ReactiveHtmlElement[HTMLAnchorElement]]) =>
          val locationState = LocationState.closestOrFail(ctx.thisNode.ref)

          // managed subscription
          val _ = EventStream
            .merge(
              EventStream.fromValue(()).sample(locationState.location),
              mutations.events.sample(locationState.location),
              locationState.location.changes
            )
            .foreach { location =>
              val UrlString(url) = ctx.thisNode.ref.href
              activeVar.set {
                location.exists { location =>
                  compare(location, url)
                }
              }
            }(ctx.owner)
          mutationObserver.observe(
            ctx.thisNode.ref,
            new MutationObserverInit {
              attributes = true
              attributeFilter = js.Array("href")
            }
          )
        },
        unmount = { (_: ReactiveHtmlElement[HTMLAnchorElement]) =>
          mutationObserver.disconnect()
        }
      ),
      mod(activeVar.signal)
    )
  }

  def navMod(
    mod: Signal[Boolean] => Mod[ReactiveHtmlElement[HTMLAnchorElement]]
  ): Mod[ReactiveHtmlElement[HTMLAnchorElement]] =
    navModFn((location, url) => location.fullPath.mkString("/", "/", "/").startsWith(url.pathname + "/"))(mod)

  def navModExact(
    mod: Signal[Boolean] => Mod[ReactiveHtmlElement[HTMLAnchorElement]]
  ): Mod[ReactiveHtmlElement[HTMLAnchorElement]] =
    navModFn((location, url) => location.fullPath.mkString("/", "/", "") == url.pathname)(mod)

}
