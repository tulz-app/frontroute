package io.frontroute

import com.raquo.laminar.api.L._
import io.frontroute.internal.PathMatchResult
import io.frontroute.internal.Util
import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Signal
import com.raquo.airstream.state.Var
import org.scalajs.dom
import app.tulz.tuplez._
import io.frontroute.Types.TypedRoute

import scala.scalajs.js

trait RouteDSL[A] extends PathMatchers with RunRoute[A] with ApplyConverters[TypedRoute[A]] {

  type Route      = TypedRoute[A]
  type Directive0 = Directive[Unit]

  class Directive[L](
    val tapply: (L => Route) => Route
  ) {
    self =>

    def flatMap[R](next: L => Directive[R]): Directive[R] = {
      Directive[R] { inner =>
        self.tapply { value => (location, previous, state) =>
          next(value).tapply(inner)(location, previous, state.enter)
        }
      }
    }

    def map[R](f: L => R): Directive[R] =
      Directive[R] { inner =>
        self.tapply { value => (location, previous, state) =>
          val mapped = f(value)
          inner(mapped)(location, previous, state.enterAndSet(mapped))
        }
      }

    @inline def some: Directive[Option[L]] = map(Some(_))

    @inline def none[R]: Directive[Option[R]] = mapTo(Option.empty[R])

    @inline def mapTo[R](otherValue: => R): Directive[R] = map(_ => otherValue)

    def &(magnet: ConjunctionMagnet[L]): magnet.Out = magnet(this)

    def |(other: Directive[L]): Directive[L] = {
      Directive[L] { inner => (location, previous, state) =>
        self
          .tapply { value => (location, previous, state) =>
            inner(value)(location, previous, state.leaveDisjunction)
          }(location, previous, state.enterDisjunction)
          .flatMap {
            case complete: RouteResult.Complete[A] => EventStream.fromValue(complete)
            case RouteResult.Rejected              =>
              other.tapply { value => (location, previous, state) =>
                inner(value)(location, previous, state.leaveDisjunction)
              }(location, previous, state.enterDisjunction)
          }
      }

    }

    def collect[R](f: PartialFunction[L, R]): Directive[R] =
      Directive[R] { inner =>
        self.tapply { value => (location, previous, state) =>
          if (f.isDefinedAt(value)) {
            val mapped = f(value)
            inner(mapped)(location, previous, state.enterAndSet(mapped))
          } else {
            Util.rejected
          }
        }
      }

    def filter(predicate: L => Boolean): Directive[L] =
      Directive[L] { inner =>
        self.tapply { value => (location, previous, state) =>
          if (predicate(value)) {
            inner(value)(location, previous, state.enter)
          } else {
            Util.rejected
          }
        }
      }

    def signal: Directive[Signal[L]] =
      new Directive[Signal[L]]({ inner => (location, previous, state) =>
        this.tapply {
          value => // TODO figure this out, when this is run, enter is not yet called
            (location, previous, state) =>
              val next = state.unsetValue().enter
              previous.getValue[Var[L]](next.path.key) match {
                case None              =>
                  val newVar = Var(value)
                  inner(newVar.signal)(location, previous, next.setValue(newVar))
                case Some(existingVar) =>
                  existingVar.set(value)
                  inner(existingVar.signal)(location, previous, next.setValue(existingVar))
              }
        }(location, previous, state)
      })

  }

  object Directive {

    def apply[L](f: (L => Route) => Route): Directive[L] = {
      new Directive[L](inner =>
        (location, previous, state) =>
          f(value =>
            (location, previous, state) => {
              inner(value)(location, previous, state)
            }
          )(location, previous, state)
      )
    }

  }

  implicit def directiveOfOptionSyntax(underlying: Directive[Option[A]]): DirectiveOfOptionOps = new DirectiveOfOptionOps(underlying)

  trait ConjunctionMagnet[L] {
    type Out
    def apply(underlying: Directive[L]): Out
  }

  object ConjunctionMagnet {

    implicit def fromDirective[L, R](other: Directive[R])(implicit composition: Composition[L, R]): ConjunctionMagnet[L] { type Out = Directive[composition.Composed] } =
      new ConjunctionMagnet[L] {
        type Out = Directive[composition.Composed]
        def apply(underlying: Directive[L]): Directive[composition.Composed] =
          Directive[composition.Composed] { inner => (location, previous, state) =>
            underlying.tapply { prefix => (location, previous, state) =>
              other.tapply { suffix =>
                inner(composition.compose(prefix, suffix))
              }(location, previous, state.enterConjunction)
            }(location, previous, state)
          }
      }

  }

  def reject: Route = (_, _, _) => Util.rejected

  private[frontroute] def extractContext: Directive[RouteLocation] =
    Directive[RouteLocation](inner => (location, previous, state) => inner(location)(location, previous, state))

  private[frontroute] def extract[T](f: RouteLocation => T): Directive[T] =
    extractContext.map(f)

  def state[T](initial: => T): Directive[T] = {
    Directive[T](inner =>
      (location, previous, state) => {
        val next = state.enter
        state.getPersistentValue[T](next.path.key) match {
          case None =>
            val newStateValue = initial
            inner(newStateValue)(location, previous, next.setPersistentValue(newStateValue))

          case Some(existing) =>
            inner(existing)(location, previous, next)
        }
      }
    )
  }

  def signal[T](signal: Signal[T]): Directive[T] = {
    Directive[T](inner =>
      (location, previous, state) => {
        signal.flatMap { extracted =>
          inner(extracted)(location, previous, state.enterAndSet(extracted))
        }
      }
    )
  }

  def memoize[T](retrieve: () => EventStream[T]): Directive[T] = {
    Directive[T](inner =>
      (location, previous, state) => {
        state.async.get((state.path.key, state.data)) match {
          case Some(value) =>
            inner(value.asInstanceOf[T])(location, previous, state.enterAndSet(value.asInstanceOf[T]))
          case _           =>
            var retrieved = 0
            retrieve()
              .filter { _ =>
                retrieved = retrieved + 1
                retrieved == 1
              }
              .flatMap { retrieved =>
                val newState = state.copy(
                  async = state.async.updated((state.path.key, state.data), retrieved)
                )
                inner(retrieved)(location, previous, newState.enterAndSet(retrieved))
              }
        }
      }
    )
  }

  def param(name: String): Directive[String] = {
    Directive[String](inner =>
      (location, previous, state) => {
        location.params.get(name).flatMap(_.headOption) match {
          case Some(paramValue) => inner(paramValue)(location, previous, state.enterAndSet(paramValue))
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
      (location, previous, state) => {
        val maybeParamValue = location.params.get(name).flatMap(_.headOption)
        inner(maybeParamValue)(location, previous, state.enterAndSet(maybeParamValue))
      }
    )

  def extractUnmatchedPath: Directive[List[String]] = extract(_.unmatchedPath)

  def extractHostname: Directive[String] = extract(_.hostname)

  def extractPort: Directive[String] = extract(_.port)

  def extractHost: Directive[String] = extract(_.host)

  def extractProtocol: Directive[String] = extract(_.protocol)

  def extractOrigin: Directive[Option[String]] = extract(_.origin)

  def provide[L](value: L): Directive[L] =
    Directive(inner => (location, previous, state) => inner(value)(location, previous, state.enterAndSet(value)))

  def pathPrefix[T](m: PathMatcher[T]): Directive[T] = {
    Directive[T](inner =>
      (location, previous, state) => {
        m(location.unmatchedPath) match {
          case PathMatchResult.Match(t, rest) => inner(t)(location.withUnmatchedPath(rest), previous, state.enterAndSet(t))
          case _                              => Util.rejected
        }
      }
    )
  }

  def testPathPrefix[T](m: PathMatcher[T]): Directive[T] = {
    Directive[T](inner =>
      (location, previous, state) => {
        m(location.unmatchedPath) match {
          case PathMatchResult.Match(t, _) => inner(t)(location, previous, state.enterAndSet(t))
          case _                           => Util.rejected
        }
      }
    )
  }

  def pathEnd: Directive0 =
    Directive[Unit](inner =>
      (location, previous, state) => {
        if (location.unmatchedPath.isEmpty) {
          inner(())(location, previous, state.enter)
        } else {
          Util.rejected
        }
      }
    )

  def path[T](m: PathMatcher[T]): Directive[T] = {
    Directive[T](inner =>
      (location, previous, state) => {
        m(location.unmatchedPath) match {
          case PathMatchResult.Match(t, Nil) => inner(t)(location.withUnmatchedPath(List.empty), previous, state.enterAndSet(t))
          case _                             => Util.rejected
        }
      }
    )
  }

  def testPath[T](m: PathMatcher[T]): Directive[T] = {
    Directive[T](inner =>
      (location, previous, state) => {
        m(location.unmatchedPath) match {
          case PathMatchResult.Match(t, Nil) => inner(t)(location, previous, state.enterAndSet(t))
          case _                             => Util.rejected
        }
      }
    )
  }

  def complete(result: => ToComplete[A]): Route = (_, _, state) =>
    EventStream.fromValue(
      RouteResult.Complete[A](state, () => result.get)
    )

  def debug(message: Any, optionalParams: Any*)(subRoute: Route): Route =
    (location, previous, state) => {
      dom.console.debug(message, optionalParams: _*)
      subRoute(location, previous, state)
    }

  def concat(routes: Route*): Route = (location, previous, state) => {
    def findFirst(rs: List[(Route, Int)]): EventStream[RouteResult[A]] =
      rs match {
        case Nil                    => Util.rejected
        case (route, index) :: tail =>
          route(location, previous, state.enterConcat(index)).flatMap {
            case complete: RouteResult.Complete[A] => EventStream.fromValue(complete)
            case RouteResult.Rejected              => findFirst(tail)
          }
      }

    findFirst(routes.zipWithIndex.toList)
  }

  implicit def toDirective[L](route: Route): Directive[L] = Directive[L](_ => route)

  class DirectiveOfOptionOps(underlying: Directive[Option[A]]) {

    @inline def mapOption[R](f: A => R): Directive[Option[R]] = underlying.map(_.map(f))

  }

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

  implicit def runRouteImplicitly(
    route: Route
  )(implicit locationProvider: LocationProvider = LocationProvider.defaultProvider, ev: A <:< Element): Mod[Element] =
    renderRoute(route)

  def renderRoute(
    route: Route
  )(implicit locationProvider: LocationProvider = LocationProvider.defaultProvider, ev: A <:< Element): Mod[Element] =
    onMountInsert { ctx =>
      import ctx.owner
      child.maybe <-- runRoute(route).asInstanceOf[Signal[Option[Element]]]
    }

}
