package io.frontroute

import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.matching.Regex
import scala.util.matching.Regex.Match
import app.tulz.tuplez.Composition
import io.frontroute.internal.PathMatchResult

abstract class PathMatcher[T] {
  self =>

  def apply(path: List[String]): PathMatchResult[T]

  def map[V](f: T => V): PathMatcher[V] =
    (in: List[String]) => self(in).map(f)

  @inline def mapTo[V](value: => V): PathMatcher[V] = self.map((_: T) => value)

  def emap[V](f: T => Either[String, V]): PathMatcher[V] =
    (in: List[String]) =>
      self(in) match {
        case PathMatchResult.NoMatch            => PathMatchResult.NoMatch
        case PathMatchResult.Rejected(tail)     => PathMatchResult.Rejected(tail)
        case PathMatchResult.Match(value, tail) =>
          f(value) match {
            case Right(result) => PathMatchResult.Match(result, tail)
            case Left(_)       => PathMatchResult.Rejected(tail)
          }
      }

  def tryParse[V](f: T => V): PathMatcher[V] =
    (in: List[String]) =>
      self(in) match {
        case PathMatchResult.NoMatch            => PathMatchResult.NoMatch
        case PathMatchResult.Rejected(tail)     => PathMatchResult.Rejected(tail)
        case PathMatchResult.Match(value, tail) =>
          Try(f(value)) match {
            case Success(result) => PathMatchResult.Match(result, tail)
            case Failure(_)      => PathMatchResult.Rejected(tail)
          }
      }

  def flatMap[V](f: T => PathMatcher[V]): PathMatcher[V] =
    (path: List[String]) =>
      self(path) match {
        case PathMatchResult.NoMatch            => PathMatchResult.NoMatch
        case PathMatchResult.Rejected(tail)     => PathMatchResult.Rejected(tail)
        case PathMatchResult.Match(value, tail) => f(value).apply(tail)
      }

  def filter(f: T => Boolean): PathMatcher[T] =
    (in: List[String]) =>
      self(in) match {
        case PathMatchResult.NoMatch            => PathMatchResult.NoMatch
        case PathMatchResult.Rejected(tail)     => PathMatchResult.Rejected(tail)
        case PathMatchResult.Match(value, tail) =>
          if (f(value)) {
            PathMatchResult.Match(value, tail)
          } else {
            PathMatchResult.Rejected(tail)
          }
      }

  def collect[V](f: PartialFunction[T, V]): PathMatcher[V] =
    (in: List[String]) =>
      self(in) match {
        case PathMatchResult.NoMatch            => PathMatchResult.NoMatch
        case PathMatchResult.Rejected(tail)     => PathMatchResult.Rejected(tail)
        case PathMatchResult.Match(value, tail) =>
          if (f.isDefinedAt(value)) {
            PathMatchResult.Match(f(value), tail)
          } else {
            PathMatchResult.Rejected(tail)
          }
      }

  def recover[V >: T](default: => V): PathMatcher[V] =
    (in: List[String]) =>
      self(in) match {
        case PathMatchResult.NoMatch            => PathMatchResult.NoMatch
        case PathMatchResult.Rejected(tail)     => PathMatchResult.Match(default, tail)
        case PathMatchResult.Match(value, tail) => PathMatchResult.Match(value, tail)
      }

  @inline def withFilter(f: T => Boolean): PathMatcher[T] = this.filter(f)

  def /[V](other: PathMatcher[V])(implicit compose: Composition[T, V]): PathMatcher[compose.Composed] =
    self.flatMap { t1 =>
      other.map { v =>
        compose.compose(t1, v)
      }
    }

  @inline def as[O](f: T => O): PathMatcher[O] = self.map(f)

  @inline def void: PathMatcher[Unit] = this.mapTo(())

  def unary_! : PathMatcher[Unit] = (path: List[String]) =>
    self(path) match {
      case PathMatchResult.NoMatch        => PathMatchResult.NoMatch
      case PathMatchResult.Rejected(tail) => PathMatchResult.Match((), tail)
      case PathMatchResult.Match(_, tail) => PathMatchResult.Rejected(tail)
    }

}

object PathMatcher {

  val unit: PathMatcher[Unit] = (path: List[String]) => PathMatchResult.Match((), path)

  def provide[V](v: V): PathMatcher[V] = unit.map(_ => v)

  def fail[T]: PathMatcher[T] = (path: List[String]) => PathMatchResult.Rejected(path)

}

trait PathMatchers {

  def segment: PathMatcher[String] = {
    case head :: tail => PathMatchResult.Match(head, tail)
    case Nil          => PathMatchResult.NoMatch
  }

  def segment(oneOf: Seq[String]): PathMatcher[String] = {
    case head :: tail =>
      if (oneOf.contains(head)) {
        PathMatchResult.Match(head, tail)
      } else {
        PathMatchResult.Rejected(tail)
      }
    case Nil          => PathMatchResult.NoMatch
  }

  def segment(oneOf: Set[String]): PathMatcher[String] = {
    case head :: tail =>
      if (oneOf.contains(head)) {
        PathMatchResult.Match(head, tail)
      } else {
        PathMatchResult.Rejected(tail)
      }
    case Nil          => PathMatchResult.NoMatch
  }

  def segment(s: String): PathMatcher0 = {
    case head :: tail =>
      if (head == s) {
        PathMatchResult.Match((), tail)
      } else {
        PathMatchResult.Rejected(tail)
      }
    case Nil          => PathMatchResult.NoMatch
  }

  def regex(r: Regex): PathMatcher[Match] =
    segment
      .map { s => r.findFirstMatchIn(s) }
      .collect { case Some(m) => m }

  def long: PathMatcher[Long] = segment.tryParse(_.toLong)

  def double: PathMatcher[Double] = segment.tryParse(_.toDouble)

  implicit def stringToSegment(s: String): PathMatcher[Unit]         = segment(s)
  implicit def setToSegment(oneOf: Set[String]): PathMatcher[String] = segment(oneOf)
  implicit def setToSegment(oneOf: Seq[String]): PathMatcher[String] = segment(oneOf)
  implicit def regexToPathMatcher(r: Regex): PathMatcher[Match]      = regex(r)

}
