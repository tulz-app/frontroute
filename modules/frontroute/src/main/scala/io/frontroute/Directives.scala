package io.frontroute

import com.raquo.laminar.api.L._
import io.frontroute.internal.PathMatchResult

import scala.scalajs.js

trait Directives {

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
          case None             => rejected
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
          case _                              => rejected
        }
      }
    )
  }

  def testPathPrefix[T](m: PathMatcher[T]): Directive[T] = {
    Directive[T](inner =>
      (location, previous, state) => {
        m(location.unmatchedPath) match {
          case PathMatchResult.Match(t, _) => inner(t)(location, previous, state.enterAndSet(t))
          case _                           => rejected
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
          rejected
        }
      }
    )

  def path[T](m: PathMatcher[T]): Directive[T] = {
    Directive[T](inner =>
      (location, previous, state) => {
        m(location.unmatchedPath) match {
          case PathMatchResult.Match(t, Nil) => inner(t)(location.withUnmatchedPath(List.empty), previous, state.enterAndSet(t))
          case _                             => rejected
        }
      }
    )
  }

  def noneMatched: Directive0 = {
    Directive[Unit](inner =>
      (location, previous, state) => {
        if (location.otherMatched) {
          rejected
        } else {
          inner(())(location, previous, state.enter)
        }
      }
    )
  }

  def testPath[T](m: PathMatcher[T]): Directive[T] = {
    Directive[T](inner =>
      (location, previous, state) => {
        m(location.unmatchedPath) match {
          case PathMatchResult.Match(t, Nil) => inner(t)(location, previous, state.enterAndSet(t))
          case _                             => rejected
        }
      }
    )
  }

  def when(condition: => Boolean): Directive0 = {
    Directive[Unit](inner =>
      (location, previous, state) => {
        if (condition) {
          inner(())(location, previous, state)
        } else {
          rejected
        }
      }
    )
  }
}
