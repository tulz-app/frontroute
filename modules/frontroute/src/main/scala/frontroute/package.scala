import com.raquo.laminar.api.L.*
import com.raquo.airstream.core.Signal
import com.raquo.laminar.nodes.ReactiveHtmlElement
import app.tulz.tuplez.ApplyConverter
import app.tulz.tuplez.ApplyConverters
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

// TODO: ApplyConverters is not needed in scala-3
package object frontroute extends PathMatchers with Directives with FrontrouteCross with ApplyConverters[Route] {

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
    initRouting(FrontrouteOptions.default)
  }

  def initRouting(options: FrontrouteOptions): Modifier[Element] = {
    initRouting(baseName = "", options)
  }

  def initRouting(baseName: BaseName): Modifier[Element] = {
    initRouting(baseName, FrontrouteOptions.default)
  }

  def initRouting(baseName: BaseName, options: FrontrouteOptions): Modifier[Element] = {
    if (baseName != "" && !(baseName.startsWith("/") && !baseName.endsWith("/")))
      throw new IllegalArgumentException("baseName must be empty; or start with /, and NOT end with /")
    initRouting(LocationProvider.windowLocationProvider(baseName), options = options)
  }

  def initRouting(lp: LocationProvider): Modifier[Element] =
    initRouting(lp, FrontrouteOptions.default)

  def initRouting(lp: LocationProvider, options: FrontrouteOptions): Modifier[Element] =
    onMountUnmountCallbackWithState(
      ctx => {
        LocationState.init(
          ctx.thisNode.ref,
          LocationState.withLocationProvider(lp)(ctx.owner)
        )
        Option.when(options.installHrefHandler) {
          HrefHandler.install(ctx, options)
        }
      },
      (_, observer: Option[MutationObserver]) => {
        observer.foreach(_.disconnect())
      }
    )

  def routes(mods: Modifier[Element]*): ReactiveHtmlElement[HTMLDivElement] =
    routes(baseName = "", FrontrouteOptions.default)(mods)

  def routes(baseName: BaseName)(mods: Modifier[Element]*): ReactiveHtmlElement[HTMLDivElement] =
    routes(baseName, FrontrouteOptions.default)(mods)

  def routes(options: FrontrouteOptions)(mods: Modifier[Element]*): ReactiveHtmlElement[HTMLDivElement] =
    routes(baseName = "", options)(mods)

  def routes(baseName: BaseName, options: FrontrouteOptions)(mods: Modifier[Element]*): ReactiveHtmlElement[HTMLDivElement] =
    div(
      styleAttr := "display: contents",
      initRouting(baseName, options),
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

  implicit def addDirectiveApply[L](directive: Directive[L])(implicit hac: ApplyConverter[L, Route]): hac.In => Route = { subRoute => (location, previous, state, baseName) =>
    directive.tapply(hac(subRoute))(location, previous, state, baseName)
  }

  implicit def addNullaryDirectiveApply(directive: => Directive0): Route => Route = { subRoute => (location, previous, state, baseName) =>
    directive.tapply(_ => subRoute)(location, previous, state, baseName)
  }

  implicit def addDirectiveExecute[L](directive: Directive[L]): DirectiveExecute[L => Unit] = new DirectiveExecute[L => Unit] {
    def execute(run: L => Unit): Route = {
      directive.tapply { l =>
        runEffect {
          run(l)
        }
      }
    }
  }

  implicit def addNullaryDirectiveExecute(directive: => Directive0): DirectiveUnitExecute = new DirectiveUnitExecute {
    def execute(run: => Unit): Route = {
      directive.tapply { _ =>
        runEffect {
          run
        }
      }
    }
  }

  private def complete(result: () => HtmlElement): Route = (location, _, state, _) => RouteResult.Matched(state, location, state.consumed, result)

  def runEffect(effect: => Unit): Route = (location, _, state, _) =>
    RouteResult.RunEffect(
      state,
      location,
      List.empty,
      () => effect
    )

  private[frontroute] def makeRelative(matched: List[String], path: String, query: Seq[(String, Seq[String])], baseName: BaseName): String = {
    val queryStr = LocationUtils.encodeLocationParams(query)
    makeRelative(matched, path, queryStr, baseName)
  }

  @tailrec
  private[frontroute] def moveUp(matchedReversed: List[String], path: String): (List[String], String) = {
    if (!path.startsWith("../")) (matchedReversed.reverse, path)
    else if (matchedReversed.isEmpty) (matchedReversed.reverse, path)
    else moveUp(matchedReversed.tail, path.drop("../".length))
  }

  private[frontroute] def makeRelative(matchedOriginal: List[String], pathOriginal: String, queryStr: String, baseName: BaseName): String = {
    val relative = {
      if (pathOriginal.startsWith("/")) {
        s"${baseName}${pathOriginal}"
      } else {
        val (matched, path) = moveUp(matchedOriginal.reverse, pathOriginal)

        if (matched.nonEmpty) {
          if (path.nonEmpty) {
            matched.mkString(s"${baseName}/", "/", s"/$path")
          } else {
            matched.mkString(s"${baseName}/", "/", "")
          }
        } else {
          if (path.nonEmpty) {
            s"/${baseName}$path"
          } else {
            s"${baseName}/"
          }
        }
      }
    }
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
    extractBaseName { baseName =>
      extractMatchedPath { matched =>
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

  def withMatchedPath[Ref <: dom.html.Element](mod: (BaseName, StrictSignal[List[String]]) => Mod[ReactiveHtmlElement[Ref]]): Mod[ReactiveHtmlElement[Ref]] = {
    onMountCallback { ctx =>
      val locationState = LocationState.closestOrFail(ctx.thisNode.ref)
      mod(locationState.baseName, locationState.consumed)(ctx.thisNode)
    }
  }

  @inline def relativeHref(path: String): Mod[ReactiveHtmlElement[html.Anchor]] =
    relativeHref(path, Seq.empty)

  def relativeHref(path: String, query: Seq[(String, Seq[String])]): Mod[ReactiveHtmlElement[html.Anchor]] =
    withMatchedPath { (baseName, matched) =>
      Seq(
        href <-- matched.map { matched =>
          makeRelative(matched, path, query, baseName)
        },
        dataAttr("fr-rewrite") := "ignore"
      )
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
              EventStream.unit().sample(locationState.location),
              mutations.events.sample(locationState.location),
              locationState.location.updates
            )
            .foreach { location =>
              val href = ctx.thisNode.ref.href
              if (href != null) {
                val UrlString(url) = href
                if (locationState.baseName != "" && url.pathname.startsWith(locationState.baseName)) {
                  url.pathname = url.pathname.drop(locationState.baseName.length)
                }
                activeVar.set {
                  location.flatMap(_.toOption).exists { location =>
                    compare(location, url)
                  }
                }
              } else {
                activeVar.set(false)
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
