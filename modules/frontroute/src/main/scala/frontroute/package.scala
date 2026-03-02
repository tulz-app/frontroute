import com.raquo.laminar.api.L.*

import com.raquo.airstream.core.Signal
import com.raquo.laminar.nodes.ReactiveElement
import com.raquo.laminar.nodes.ReactiveHtmlElement
import frontroute.internal.LocationState
import frontroute.internal.UrlString
import org.scalajs.dom
import org.scalajs.dom.HTMLAnchorElement
import org.scalajs.dom.HTMLDivElement
import org.scalajs.dom.MutationObserver
import org.scalajs.dom.MutationObserverInit
import org.scalajs.dom.MutationRecord
import org.scalajs.dom.html

import scala.annotation.tailrec
import scala.scalajs.js

package object frontroute extends PathMatchers with Directives with FrontrouteCross {

  type PathMatcher0 = PathMatcher[Unit]

  type Directive0 = Directive[Unit]

  type BaseName = String

  private[frontroute] val rejected: RouteResult = RouteResult.Rejected

  val reject: Route = (_, _, _, _) => rejected

  def debug(message: Any, optionalParams: Any*)(subRoute: Route): Route = { (location, previous, state, baseName) =>
    dom.console.debug(message, optionalParams*)
    subRoute(location, previous, state, baseName)
  }

  @deprecated("use firstMatch instead", "0.16.0")
  def concat(routes: Route*): Route = firstMatch(routes*)

  def initRouting: Modifier[Element] = {
    initRouting(LocationProvider.windowLocationProvider)
  }

  def initRouting(baseName: BaseName): Modifier[Element] = {
    if (baseName != "" && !(baseName.startsWith("/") && !baseName.endsWith("/")))
      throw new IllegalArgumentException("baseName must be empty; or start with /, and NOT end with /")
    initRouting(LocationProvider.windowLocationProvider(baseName))
  }

  def initRouting(lp: LocationProvider): Modifier[Element] =
    onMountCallback { ctx =>
      LocationState.init(
        ctx.thisNode.ref,
        LocationState.withLocationProvider(lp)(ctx.owner)
      )
    }

  def routes(mods: Modifier[Element]*): ReactiveHtmlElement[HTMLDivElement] =
    div(
      styleAttr := "display: contents",
      initRouting,
      mods
    )

  def routes(baseName: BaseName)(mods: Modifier[Element]*): ReactiveHtmlElement[HTMLDivElement] =
    div(
      styleAttr := "display: contents",
      initRouting(baseName),
      mods
    )

  def firstMatch(routes: Route*): Route = (location, previous, state, baseName) => {

    @tailrec
    def findFirst(rs: List[(Route, Int)]): RouteResult =
      rs match {
        case Nil                    => rejected
        case (route, index) :: tail =>
          route(location, previous, state.enterConcat(index), baseName) match {
            case RouteResult.Matched(state, location, consumed, result) => RouteResult.Matched(state, location, consumed, result)
            case RouteResult.RunEffect(state, location, consumed, run)  => RouteResult.RunEffect(state, location, consumed, run)
            case RouteResult.Rejected                                   => findFirst(tail)
          }
      }

    findFirst(routes.zipWithIndex.toList)
  }

  private def complete(result: () => HtmlElement): Route = (location, _, state, _) => RouteResult.Matched(state, location, state.consumed, result)

  def runEffect(effect: => Unit): Route = (location, _, state, _) =>
    RouteResult.RunEffect(
      state,
      location,
      List.empty,
      () => effect
    )

  private def makeRelative(matched: List[String], path: String, query: Seq[(String, Seq[String])], baseName: String): String = {
    val relative = {
      if (path.startsWith("/")) {
        baseName + path
      } else if (matched.nonEmpty) {
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
    }
    val queryStr = LocationUtils.encodeLocationParams(query)
    if (queryStr.nonEmpty) {
      s"$relative$queryStr"
    } else {
      relative
    }
  }

  @inline def navigate(
    to: String,
  ): Route =
    navigate(to, Seq.empty, replace = false)

  @inline def navigate(
    to: String,
    replace: Boolean,
  ): Route =
    navigate(to, Seq.empty, replace)

  @inline def navigate(
    to: String,
    query: Map[String, Seq[String]],
  ): Route =
    navigate(to, query.toSeq, replace = false)

  @inline def navigate(
    to: String,
    query: Map[String, Seq[String]],
    replace: Boolean,
  ): Route =
    navigate(to, query.toSeq, replace)

  @inline def navigate(
    to: String,
    query: Seq[(String, Seq[String])],
  ): Route =
    navigate(to, query, replace = false)

  def navigate(
    to: String,
    query: Seq[(String, Seq[String])],
    replace: Boolean,
  ): Route = {
    extractBaseName { (baseName: BaseName) =>
      extractMatchedPath { (matched: List[String]) =>
        val relative = makeRelative(matched, to, query, baseName)
        runEffect {
          if (replace) {
            BrowserNavigation.replaceState(url = relative)
          } else {
            BrowserNavigation.pushState(url = relative)
          }
        }
      }
    }
  }

  implicit def elementToRoute(e: => HtmlElement): Route = complete(() => e)

  def withMatchedPath[Ref <: dom.html.Element](mod: (StrictSignal[BaseName], StrictSignal[List[String]]) => Mod[ReactiveHtmlElement[Ref]]): Mod[ReactiveHtmlElement[Ref]] = {
    val consumedVar = Var(List.empty[String])
    val baseName    = Var("")
    Seq(
      onMountCallback { (ctx: MountContext[ReactiveHtmlElement[Ref]]) =>
        val locationState = LocationState.closestOrFail(ctx.thisNode.ref)
        val consumed      = EventStream.fromValue(()).delay(0).sample(locationState.consumed)
        val _             = ReactiveElement.bindObserver(ctx.thisNode, consumed)(consumedVar.writer)
        baseName.set(locationState.baseName)
      },
      mod(baseName.signal.debugLogEvents(), consumedVar.signal)
    )
  }

  @inline def relativeHref(path: String): Mod[ReactiveHtmlElement[html.Anchor]] =
    relativeHref(path, Seq.empty)

  def relativeHref(path: String, query: Seq[(String, Seq[String])]): Mod[ReactiveHtmlElement[html.Anchor]] =
    withMatchedPath { (baseName, matched) =>
      href <-- matched.combineWithFn(baseName) { (matched, baseName) =>
        println(s"relativeHref: $path, baseName: $baseName")
        makeRelative(matched, path, query, baseName)
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
              locationState.location.updates
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
