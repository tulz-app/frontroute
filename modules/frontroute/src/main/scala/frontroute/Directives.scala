package frontroute

import frontroute.internal.PathMatchResult

import scala.scalajs.js

trait Directives {

  private[frontroute] val extractLocation: Directive[Location] =
    new Directive[Location](inner => (location, previous, state, baseName) => inner(location)(location, previous, state, baseName))

  private[frontroute] val extractBaseName: Directive[String] =
    new Directive[String](inner => (location, previous, state, baseName) => inner(baseName)(location, previous, state, baseName))

  private[frontroute] def extract[T](f: Location => T): Directive[T] =
    extractLocation.map(f)

  def param(name: String): Directive[String] =
    Directive[String] { inner => (location, previous, state, baseName) =>
      location.params.get(name).flatMap(_.headOption) match {
        case Some(paramValue) => inner(paramValue)(location, previous, state.enterAndSet(paramValue), baseName)
        case None             => rejected
      }
    }

  def multiParam(name: String): Directive[Seq[String]] =
    Directive[Seq[String]] { inner => (location, previous, state, baseName) =>
      val values = location.params.getOrElse(name, Seq.empty)
      inner(values)(location, previous, state.enterAndSet(values), baseName)
    }

  val historyState: Directive[Option[js.Any]] =
    extractLocation.map(_.parsedState.flatMap(_.user.toOption))

  val historyScroll: Directive[Option[ScrollPosition]] =
    extractLocation.map(_.parsedState.flatMap(_.internal.toOption).flatMap(_.scroll.toOption).map { scroll =>
      ScrollPosition(
        scrollX = scroll.scrollX.toOption.map(_.round.toInt),
        scrollY = scroll.scrollY.toOption.map(_.round.toInt)
      )
    })

  def maybeParam(name: String): Directive[Option[String]] =
    Directive[Option[String]] { inner => (location, previous, state, baseName) =>
      val maybeParamValue = location.params.get(name).flatMap(_.headOption)
      inner(maybeParamValue)(location, previous, state.enterAndSet(maybeParamValue), baseName)
    }

  val extractMatchedPath: Directive[List[String]] =
    new Directive[List[String]](inner => (location, previous, state, baseName) => inner(state.consumed)(location, previous, state, baseName))

  val extractUnmatchedPath: Directive[List[String]] = extract(_.path)

  val extractHostname: Directive[String] = extract(_.hostname)

  val extractPort: Directive[String] = extract(_.port)

  val extractHost: Directive[String] = extract(_.host)

  val extractProtocol: Directive[String] = extract(_.protocol)

  val extractOrigin: Directive[String] = extract(_.origin)

  def provide[L](value: L): Directive[L] =
    Directive { inner => (location, previous, state, baseName) =>
      inner(value)(location, previous, state.enterAndSet(value), baseName)
    }

  def provideOption[L](value: Option[L]): Directive[L] =
    Directive { inner => (location, previous, state, baseName) =>
      value match {
        case None        => rejected
        case Some(value) => inner(value)(location, previous, state.enterAndSet(value), baseName)
      }
    }

  def pathPrefix[T](m: PathMatcher[T]): Directive[T] =
    Directive[T] { inner => (location, previous, state, baseName) =>
      m(state.consumed, location.path) match {
        case PathMatchResult.Match(t, consumed, rest) =>
          inner(t)(location.withUnmatchedPath(rest), previous, state.enterAndSet(t).withConsumed(consumed), baseName)
        case _                                        => rejected
      }
    }

  def testPathPrefix[T](m: PathMatcher[T]): Directive[T] =
    Directive[T] { inner => (location, previous, state, baseName) =>
      m(state.consumed, location.path) match {
        case PathMatchResult.Match(t, _, _) => inner(t)(location, previous, state.enterAndSet(t), baseName)
        case _                              => rejected
      }
    }

  val pathEnd: Directive0 =
    Directive[Unit] { inner => (location, previous, state, baseName) =>
      if (location.path.isEmpty) {
        inner(())(location, previous, state.enter, baseName)
      } else {
        rejected
      }
    }

  def path[T](m: PathMatcher[T]): Directive[T] =
    Directive[T] { inner => (location, previous, state, baseName) =>
      m(state.consumed, location.path) match {
        case PathMatchResult.Match(t, consumed, Nil) =>
          inner(t)(location.withUnmatchedPath(List.empty), previous, state.enterAndSet(t).withConsumed(consumed), baseName)
        case _                                       => rejected
      }
    }

  def testPath[T](m: PathMatcher[T]): Directive[T] =
    Directive[T] { inner => (location, previous, state, baseName) =>
      m(state.consumed, location.path) match {
        case PathMatchResult.Match(t, _, Nil) => inner(t)(location, previous, state.enterAndSet(t), baseName)
        case _                                => rejected
      }
    }

  val noneMatched: Directive0 =
    Directive[Unit] { inner => (location, previous, state, baseName) =>
      if (location.otherMatched) {
        rejected
      } else {
        inner(())(location, previous, state.enter, baseName)
      }
    }

  def whenTrue(condition: => Boolean): Directive0 =
    Directive[Unit] { inner => (location, previous, state, baseName) =>
      if (condition) {
        inner(())(location, previous, state, baseName)
      } else {
        rejected
      }
    }

  @inline def whenFalse(condition: => Boolean): Directive0 = whenTrue(!condition)

}
