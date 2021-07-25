package io.frontroute

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Signal
import io.frontroute.internal.PathMatchResult
import io.frontroute.internal.Util
import org.scalajs.dom

import scala.scalajs.js

trait Directives extends DirectiveApplyConverters {

  def reject: Route = (_, _, _) => Util.rejected

  private[frontroute] def extractContext: Directive[RouteLocation] =
    Directive[RouteLocation](inner => (ctx, previous, state) => inner(ctx)(ctx, previous, state))

  private[frontroute] def extract[T](f: RouteLocation => T): Directive[T] =
    extractContext.map(f)

  def state[T](initial: => T): Directive[T] = {
    Directive[T](inner =>
      (ctx, previous, state) => {
        val next = state.enter
        state.getPersistentValue[T](next.path.key) match {
          case None =>
            val newStateValue = initial
            inner(newStateValue)(ctx, previous, next.setPersistentValue(newStateValue))

          case Some(existing) =>
            inner(existing)(ctx, previous, next)
        }
      }
    )
  }

  def signal[T](signal: Signal[T]): Directive[T] = {
    Directive[T](inner =>
      (ctx, previous, state) => {
        signal.flatMap { extracted =>
          inner(extracted)(ctx, previous, state.enterAndSet(extracted))
        }
      }
    )
  }

  def param(name: String): Directive[String] = {
    Directive[String](inner =>
      (ctx, previous, state) => {
        ctx.params.get(name).flatMap(_.headOption) match {
          case Some(paramValue) => inner(paramValue)(ctx, previous, state.enterAndSet(paramValue))
          case None             => Util.rejected
        }
      }
    )
  }

  def historyState: Directive[Option[js.Any]] = {
    extractContext.map(_.parsedState.flatMap(_.user.toOption))
  }

  def historyScroll: Directive[Option[ScrollPosition]] = {
    extractContext.map(_.parsedState.flatMap(_.internal.toOption).flatMap(_.scroll.toOption).map { scroll =>
      ScrollPosition(
        scrollX = scroll.scrollX.toOption.map(_.round.toInt),
        scrollY = scroll.scrollY.toOption.map(_.round.toInt)
      )
    })
  }

  def maybeParam(name: String): Directive[Option[String]] =
    Directive[Option[String]](inner =>
      (ctx, previous, state) => {
        val maybeParamValue = ctx.params.get(name).flatMap(_.headOption)
        inner(maybeParamValue)(ctx, previous, state.enterAndSet(maybeParamValue))
      }
    )

  def extractUnmatchedPath: Directive[List[String]] = extract(_.unmatchedPath)

  def extractHostname: Directive[String] = extract(_.hostname)

  def extractPort: Directive[String] = extract(_.port)

  def extractHost: Directive[String] = extract(_.host)

  def extractProtocol: Directive[String] = extract(_.protocol)

  def extractOrigin: Directive[Option[String]] = extract(_.origin)

  def provide[L](value: L): Directive[L] =
    Directive(inner => (ctx, previous, state) => inner(value)(ctx, previous, state.enterAndSet(value)))

  def pathPrefix[T](m: PathMatcher[T]): Directive[T] = {
    Directive[T](inner =>
      (ctx, previous, state) => {
        m(ctx.unmatchedPath) match {
          case PathMatchResult.Match(t, rest) => inner(t)(ctx.withUnmatchedPath(rest), previous, state.enterAndSet(t))
          case _                              => Util.rejected
        }
      }
    )
  }

  def testPathPrefix[T](m: PathMatcher[T]): Directive[T] = {
    Directive[T](inner =>
      (ctx, previous, state) => {
        m(ctx.unmatchedPath) match {
          case PathMatchResult.Match(t, _) => inner(t)(ctx, previous, state.enterAndSet(t))
          case _                           => Util.rejected
        }
      }
    )
  }

  def pathEnd: Directive0 =
    Directive[Unit](inner =>
      (ctx, previous, state) => {
        if (ctx.unmatchedPath.isEmpty) {
          inner(())(ctx, previous, state.enter)
        } else {
          Util.rejected
        }
      }
    )

  def path[T](m: PathMatcher[T]): Directive[T] = {
    Directive[T](inner =>
      (ctx, previous, state) => {
        m(ctx.unmatchedPath) match {
          case PathMatchResult.Match(t, Nil) => inner(t)(ctx.withUnmatchedPath(List.empty), previous, state.enterAndSet(t))
          case _                             => Util.rejected
        }
      }
    )
  }

  def testPath[T](m: PathMatcher[T]): Directive[T] = {
    Directive[T](inner =>
      (ctx, previous, state) => {
        m(ctx.unmatchedPath) match {
          case PathMatchResult.Match(t, Nil) => inner(t)(ctx, previous, state.enterAndSet(t))
          case _                             => Util.rejected
        }
      }
    )
  }

  def completeN[T](events: => EventStream[() => Unit]): Route = (_, _, state) => EventStream.fromValue(RouteResult.Complete(state, events))

  def complete[T](action: => Unit): Route = (_, _, state) =>
    EventStream.fromValue(
      RouteResult.Complete(state, EventStream.fromValue(() => action))
    )

  def debug(message: Any, optionalParams: Any*)(subRoute: Route): Route =
    (ctx, previous, state) => {
      dom.console.debug(message, optionalParams: _*)
      subRoute(ctx, previous, state)
    }

  def concat(routes: Route*): Route = (ctx, previous, state) => {
    def findFirst(rs: List[(Route, Int)]): EventStream[RouteResult] =
      rs match {
        case Nil => Util.rejected
        case (route, index) :: tail =>
          route(ctx, previous, state.enterConcat(index)).flatMap {
            case complete: RouteResult.Complete => EventStream.fromValue(complete)
            case RouteResult.Rejected           => findFirst(tail)
          }
      }

    findFirst(routes.zipWithIndex.toList)
  }

  implicit def toDirective[L](route: Route): Directive[L] = Directive[L](_ => route)

}
