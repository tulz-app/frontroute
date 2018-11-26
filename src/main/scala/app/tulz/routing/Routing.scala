package app.tulz.routing

import app.tulz.routing.TupleComposition.Composition
import com.raquo.airstream.signal.{Signal, Var}

class Routing[Ctx] extends PathMatchers {

  def runRoute[Render](root: Directive[Render], locations: Signal[Loc], contexts: Signal[Ctx]): Signal[Option[Render]] = {
    val routingContext = new RoutingContext
    (locations combineWith contexts).map {
      case (loc, ctx) =>
        root.run(loc, ctx, routingContext) match {
          case DirectiveResult.Matched(r, _, _) =>
            routingContext.roll()
            Some(r)
          case DirectiveResult.Missed =>
            routingContext.roll()
            None
        }
    }
  }

  sealed trait DirectiveResult[+T]

  object DirectiveResult {

    case class Matched[T](t: T, nextLoc: Loc, nextCtx: Ctx) extends DirectiveResult[T]
    case object Missed                                      extends DirectiveResult[Nothing]

  }

  abstract class Directive[+T](_id: String) {
    self =>

    private var parent: Option[Directive[_]] = None

    def id: DirectiveId = parent.map(parent => s"${parent.id}.${_id}").getOrElse(_id)

    def setParent(parent: Directive[_]): Unit = {
      this.parent match {
        case None    => this.parent = Some(parent)
        case Some(p) => p.setParent(parent)
      }
    }

    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[T]

    def flatMap[R](next: T => Directive[R]): Directive[R] = new FlatMapDirective[T, R](self, next)

    def map[R](f: T => R): Directive[R] = new MapDirective[T, R](self, f)

    def /[V](d: T => Directive[V]): Directive[V] = flatMap(d)

    def sub[V](d: T => Directive[V]): Directive[V] = flatMap(d)

    def &[V, U >: T](d: Directive[V])(implicit composition: Composition[U, V]): CombineDirective[U, V, composition.C] =
      new CombineDirective[U, V, composition.C](self, d, composition)

    def collect[R](f: PartialFunction[T, R]): Directive[R] = new CollectDirective[T, R](self, f)

    def filter(predicate: T => Boolean): Directive[T] = new FilterDirective[T](self, predicate)

    def maybeOverrideC[U >: T](overrideWith: PartialFunction[Ctx, U]): Directive[U] = new MaybeOverrideCDirective[T, U](self, overrideWith)

    def maybeOverride[U >: T](overrideWith: Ctx => Option[U]): Directive[U] = new MaybeOverrideDirective[T, U](self, overrideWith)

    def mapTo[R](newValue: => R): Directive[R] =
      self.map(_ => newValue)

    def ~[U >: T](anotherRoute: Directive[U]): Directive[U] = new AlternativeDirective[T, U](self, anotherRoute)

    def signal: Directive[Signal[T]] = new SignalDirective[T](self)

  }

  class FlatMapDirective[T, R](
    self: Directive[T],
    next: T => Directive[R]
  ) extends Directive[R]("fmap") {

    setParent(self)

    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[R] = {
      self.run(loc, ctx, rctx) match {
        case DirectiveResult.Missed => DirectiveResult.Missed
        case DirectiveResult.Matched(t, nextLoc, nextCtx) =>
          val nextDirective = next(t)
          nextDirective.setParent(this)
          nextDirective.run(nextLoc, nextCtx, rctx)
      }
    }
  }

  class MapDirective[T, R](
    self: Directive[T],
    project: T => R
  ) extends Directive[R]("map") {

    setParent(self)

    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[R] =
      self.run(loc, ctx, rctx) match {
        case DirectiveResult.Missed => DirectiveResult.Missed
        case DirectiveResult.Matched(t, nextLoc, nextCtx) =>
          DirectiveResult.Matched(project(t), nextLoc, nextCtx)
      }

  }

  class CollectDirective[T, R](
    self: Directive[T],
    f: PartialFunction[T, R]
  ) extends Directive[R]("collect") {

    setParent(self)

    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[R] =
      self.run(loc, ctx, rctx) match {
        case DirectiveResult.Matched(t, nextLoc, nextCtx) =>
          if (f.isDefinedAt(t)) {
            DirectiveResult.Matched(f(t), nextLoc, nextCtx)
          } else {
            DirectiveResult.Missed
          }
        case DirectiveResult.Missed => DirectiveResult.Missed

      }
  }

  class FilterDirective[T](
    self: Directive[T],
    predicate: T => Boolean
  ) extends Directive[T]("filter") {

    setParent(self)

    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[T] =
      self.run(loc, ctx, rctx) match {
        case DirectiveResult.Matched(t, nextLoc, nextCtx) =>
          if (predicate(t)) {
            DirectiveResult.Matched(t, nextLoc, nextCtx)
          } else {
            DirectiveResult.Missed
          }
        case DirectiveResult.Missed => DirectiveResult.Missed
      }

  }

  class MaybeOverrideCDirective[T, U >: T](
    self: Directive[T],
    overrideWith: PartialFunction[Ctx, U]
  ) extends Directive[U]("moc") {

    setParent(self)

    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[U] = {
      self.run(loc, ctx, rctx) match {
        case DirectiveResult.Missed => DirectiveResult.Missed
        case DirectiveResult.Matched(t, nextLoc, nextCtx) =>
          if (overrideWith.isDefinedAt(ctx)) {
            DirectiveResult.Matched(overrideWith(ctx), nextLoc, nextCtx)
          } else {
            DirectiveResult.Matched(t, nextLoc, nextCtx)
          }
      }
    }

  }

  class MaybeOverrideDirective[T, U >: T](
    self: Directive[T],
    overrideWith: Ctx => Option[U]
  ) extends Directive[U]("mo") {

    setParent(self)

    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[U] = {
      self.run(loc, ctx, rctx) match {
        case DirectiveResult.Missed => DirectiveResult.Missed
        case DirectiveResult.Matched(t, nextLoc, nextCtx) =>
          overrideWith(ctx) match {
            case None       => DirectiveResult.Matched(t, nextLoc, nextCtx)
            case Some(ovrd) => DirectiveResult.Matched(ovrd, nextLoc, nextCtx)
          }
      }
    }

  }

  class AlternativeDirective[T, U >: T](
    self: Directive[T],
    anotherRoute: Directive[U]
  ) extends Directive[U](s"~") {

    setParent(self)

    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[U] =
      self.run(loc, ctx, rctx) match {
        case DirectiveResult.Matched(t, nextLoc, nextCtx) => DirectiveResult.Matched(t, nextLoc, nextCtx)
        case DirectiveResult.Missed                       => anotherRoute.run(loc, ctx, rctx)
      }
  }

  class SignalDirective[T](
    self: Directive[T]
  ) extends Directive[Signal[T]]("signal") {

    setParent(self)

    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[Signal[T]] = {
      self.run(loc, ctx, rctx) match {
        case DirectiveResult.Missed =>
          DirectiveResult.Missed
        case DirectiveResult.Matched(t, nextLoc, nextCtx) =>
          val $var = rctx.get[Var[T]](this.id) match {
            case None => Var(t)
            case Some(v) =>
              v.writer.onNext(t)
              v
          }
          rctx.put(this.id, $var)
          DirectiveResult.Matched($var.signal, nextLoc.copy(path = Nil), nextCtx)
      }

    }

  }

  class CombineDirective[T, V, C](
    self: Directive[T],
    d: Directive[V],
    composition: Composition[T, V]
  ) extends Directive[C]("&") {

    setParent(self)

    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[C] = {
      self.run(loc, ctx, rctx) match {
        case DirectiveResult.Missed => DirectiveResult.Missed
        case DirectiveResult.Matched(t, nextLoc, nextCtx) =>
          d.run(nextLoc, nextCtx, rctx) match {
            case DirectiveResult.Missed                         => DirectiveResult.Missed
            case DirectiveResult.Matched(v, nextLoc2, nextCtx2) => DirectiveResult.Matched(composition.gc(t, v).asInstanceOf[C], nextLoc2, nextCtx2)
          }
      }
    }

  }

  def pathPrefix[T](m: PathMatcher[T]): Directive[T] = new Directive[T]("pathPrefix") {

    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[T] = {
      m.apply(loc.path) match {
        case Left(_)          => DirectiveResult.Missed
        case Right((t, tail)) => DirectiveResult.Matched(t, loc.copy(path = tail), ctx)
      }
    }
  }

  def pathEnd: Directive[Unit] = new Directive[Unit]("pathEnd") {

    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[Unit] =
      loc.path match {
        case Nil => DirectiveResult.Matched((), loc, ctx)
        case _   => DirectiveResult.Missed
      }
  }

  def path[T](m: PathMatcher[T]): Directive[T] = new Directive[T]("path") {
    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[T] = {
      m.apply(loc.path) match {
        case Left(_) => DirectiveResult.Missed
        case Right((t, tail)) =>
          if (tail.isEmpty) {
            DirectiveResult.Matched(t, loc.copy(path = tail), ctx)
          } else {
            DirectiveResult.Missed
          }
      }

    }
  }

  def just[T](t: T): Directive[T] = new Directive[T]("just") {
    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[T] = DirectiveResult.Matched(t, loc, ctx)
  }

  def param(name: Symbol): Directive[String] =
    paramOpt(name).collect {
      case Some(value) => value
    }

  def paramOpt(name: Symbol): Directive[Option[String]] = new Directive[Option[String]]("paramOpt") {

    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[Option[String]] =
      DirectiveResult.Matched(loc.params.get(name.name).flatMap(_.headOption), loc, ctx)
  }

  def extract[T](f: Ctx => T): Directive[T] = new Directive[T]("extract") {

    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[T] =
      DirectiveResult.Matched(f(ctx), loc, ctx)

  }

  def collect[T](f: PartialFunction[Ctx, T]): Directive[T] =
    extract(identity).collect(f)

  def unmatchedLoc: Directive[Loc] = new Directive[Loc]("unmatchedLoc") {

    def run(loc: Loc, ctx: Ctx, rctx: RoutingContext): DirectiveResult[Loc] = {
      DirectiveResult.Matched(loc, loc.copy(path = Nil), ctx)
    }

  }

}
