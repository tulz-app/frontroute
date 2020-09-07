package app.tulz.routing

import app.tulz.util.Tuple
import com.raquo.airstream.eventstream.EventStream
import com.raquo.airstream.signal.Signal
import com.raquo.airstream.signal.Val

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

trait Directives {

  def reject: Route = (_, _, _) => EventStream.fromValue(RouteResult.Rejected, emitOnce = true)

  def extractContext: Directive1[RouteLocation] =
    Directive[Tuple1[RouteLocation]](
      inner => (ctx, previous, state) => inner(Tuple1(ctx))(ctx, previous, state)
    )

  private[routing] def extract[T](f: RouteLocation => T): Directive1[T] =
    Directive[Tuple1[T]](
      inner => (ctx, previous, state) => inner(Tuple1(f(ctx)))(ctx, previous, state)
    )

  def signal[T](signal: Signal[T]): Directive1[T] = {
    Directive[Tuple1[T]](
      inner =>
        (ctx, previous, state) => {
          signal.flatMap { extracted =>
            inner(Tuple1(extracted))(ctx, previous, state.enter(".signal"))
          }
        }
    )
  }

  def param(name: String): Directive1[String] = {
    Directive[Tuple1[String]](
      inner =>
        (ctx, previous, state) => {
          ctx.params.get(name).flatMap(_.headOption) match {
            case Some(paramValue) => inner(Tuple1(paramValue))(ctx, previous, state.enter(s"param($name)"))
            case None             => EventStream.fromValue(RouteResult.Rejected, emitOnce = true)
          }
        }
    )
  }

  def maybeParam(name: String): Directive1[Option[String]] =
    Directive[Tuple1[Option[String]]](
      inner =>
        (ctx, previous, state) => {
          val maybeParamValue = ctx.params.get(name).flatMap(_.headOption)
          inner(Tuple1(maybeParamValue))(ctx, previous, state.enter(s"maybeParam($name)"))
        }
    )

  def extractUnmatchedPath: Directive1[List[String]] =
    extract(ctx => ctx.unmatchedPath)

  def tprovide[L: Tuple](value: L): Directive[L] =
    Directive(inner => inner(value))

  def provide[L](value: L): Directive1[L] =
    tprovide(Tuple1(value))

  def pathPrefix[T](m: PathMatcher[T]): Directive[T] = {
    import m.tuple
    Directive[T](
      inner =>
        (ctx, previous, state) => {
          m(ctx.unmatchedPath) match {
            case Right((t, rest)) => inner(t)(ctx.withUnmatchedPath(rest), previous, state.enter(s"pathPrefix($m)"))
            case _                => EventStream.fromValue(RouteResult.Rejected, emitOnce = true)
          }
        }
    )
  }

  def pathEnd: Directive0 =
    Directive[Unit](
      inner =>
        (ctx, previous, state) => {
          if (ctx.unmatchedPath.isEmpty) {
            inner(())(ctx, previous, state.enter("path-end"))
          } else {
            EventStream.fromValue(RouteResult.Rejected, emitOnce = true)
          }
        }
    )

  def path[T](m: PathMatcher[T]): Directive[T] = {
    import m.tuple
    Directive[T](
      inner =>
        (ctx, previous, state) => {
          m(ctx.unmatchedPath) match {
            case Right((t, Nil)) => inner(t)(ctx.withUnmatchedPath(List.empty), previous, state.enter(s"path($m)"))
            case _               => EventStream.fromValue(RouteResult.Rejected, emitOnce = true)
          }
        }
    )
  }

  def completeN[T](events: EventStream[() => Unit])(implicit ec: ExecutionContext): Route = { (_, _, state) =>
    EventStream.fromValue(
      RouteResult.Complete(state, events),
      emitOnce = true
    )
  }

  def complete[T](action: => Unit)(implicit ec: ExecutionContext): Route = { (_, _, state) =>
    EventStream.fromValue(
      RouteResult.Complete(state, EventStream.fromValue(() => action, emitOnce = true)),
      emitOnce = true
    )
  }

  def debug(message: => String)(subRoute: Route): Route =
    (ctx, previous, state) => {
      println(message)
      subRoute(ctx, previous, state)
    }

  def concat(routes: Route*): Route = (ctx, previous, state) => {
    def findFirst(rs: List[(Route, Int)]): EventStream[RouteResult] =
      rs match {
        case Nil => EventStream.fromValue(RouteResult.Rejected, emitOnce = true)
        case (route, index) :: tail =>
          state.enter(index.toString)
          route(ctx, previous, state).flatMap {
            case complete: RouteResult.Complete => EventStream.fromValue(complete, emitOnce = true)
            case RouteResult.Rejected           => findFirst(tail)
          }
      }

    findFirst(routes.zipWithIndex.toList)
  }

}
