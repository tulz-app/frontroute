package frontroute

import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.matching.Regex
import scala.util.matching.Regex.Match
import app.tulz.tuplez.Composition
import frontroute.internal.PathMatchResult

abstract class PathMatcher[T] {
  self =>

  def apply(
    consumed: List[String],
    path: List[String]
  ): PathMatchResult[T]

  def map[V](f: T => V): PathMatcher[V] =
    (consumed: List[String], in: List[String]) => self(consumed, in).map(f)

  @inline def mapTo[V](value: => V): PathMatcher[V] = self.map((_: T) => value)

  def emap[V](f: T => Either[String, V]): PathMatcher[V] =
    (consumed: List[String], in: List[String]) =>
      self(consumed, in) match {
        case PathMatchResult.NoMatch                      => PathMatchResult.NoMatch
        case PathMatchResult.Rejected(tail)               => PathMatchResult.Rejected(tail)
        case PathMatchResult.Match(value, consumed, tail) =>
          f(value) match {
            case Right(result) => PathMatchResult.Match(result, consumed, tail)
            case Left(_)       => PathMatchResult.Rejected(tail)
          }
      }

  def tryParse[V](f: T => V): PathMatcher[V] =
    (consumed: List[String], in: List[String]) =>
      self(consumed, in) match {
        case PathMatchResult.NoMatch                      => PathMatchResult.NoMatch
        case PathMatchResult.Rejected(tail)               => PathMatchResult.Rejected(tail)
        case PathMatchResult.Match(value, consumed, tail) =>
          Try(f(value)) match {
            case Success(result) => PathMatchResult.Match(result, consumed, tail)
            case Failure(_)      => PathMatchResult.Rejected(tail)
          }
      }

  def flatMap[V](f: T => PathMatcher[V]): PathMatcher[V] =
    (consumed: List[String], in: List[String]) =>
      self(consumed, in) match {
        case PathMatchResult.NoMatch                      => PathMatchResult.NoMatch
        case PathMatchResult.Rejected(tail)               => PathMatchResult.Rejected(tail)
        case PathMatchResult.Match(value, consumed, tail) => f(value).apply(consumed, tail)
      }

  def filter(f: T => Boolean): PathMatcher[T] =
    (consumed: List[String], in: List[String]) =>
      self(consumed, in) match {
        case PathMatchResult.NoMatch                      => PathMatchResult.NoMatch
        case PathMatchResult.Rejected(tail)               => PathMatchResult.Rejected(tail)
        case PathMatchResult.Match(value, consumed, tail) =>
          if (f(value)) {
            PathMatchResult.Match(value, consumed, tail)
          } else {
            PathMatchResult.Rejected(tail)
          }
      }

  def collect[V](f: PartialFunction[T, V]): PathMatcher[V] =
    (consumed: List[String], in: List[String]) =>
      self(consumed, in) match {
        case PathMatchResult.NoMatch                      => PathMatchResult.NoMatch
        case PathMatchResult.Rejected(tail)               => PathMatchResult.Rejected(tail)
        case PathMatchResult.Match(value, consumed, tail) =>
          if (f.isDefinedAt(value)) {
            PathMatchResult.Match(f(value), consumed, tail)
          } else {
            PathMatchResult.Rejected(tail)
          }
      }

  def recover[V >: T](default: => V): PathMatcher[V] =
    (consumed: List[String], in: List[String]) =>
      self(consumed, in) match {
        case PathMatchResult.NoMatch                      => PathMatchResult.NoMatch
        case PathMatchResult.Rejected(tail)               => PathMatchResult.Match(default, consumed, tail)
        case PathMatchResult.Match(value, consumed, tail) => PathMatchResult.Match(value, consumed, tail)
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

  def unary_! : PathMatcher[Unit] =
    (consumed: List[String], in: List[String]) =>
      self(consumed, in) match {
        case PathMatchResult.NoMatch           => PathMatchResult.NoMatch
        case PathMatchResult.Rejected(tail)    => PathMatchResult.Match((), consumed, tail)
        case PathMatchResult.Match(_, _, tail) => PathMatchResult.Rejected(tail)
      }

}

object PathMatcher {

  val unit: PathMatcher[Unit] = (consumed: List[String], in: List[String]) => PathMatchResult.Match((), consumed, in)

  def provide[V](v: V): PathMatcher[V] = unit.map(_ => v)

  def fail[T]: PathMatcher[T] = (consumed: List[String], in: List[String]) => PathMatchResult.Rejected(in)

}

trait PathMatchers {

  def segment: PathMatcher[String] =
    (consumed: List[String], in: List[String]) =>
      in match {
        case head :: tail => PathMatchResult.Match(head, consumed.appended(head), tail)
        case Nil          => PathMatchResult.NoMatch
      }

  def segment(oneOf: Seq[String]): PathMatcher[String] =
    (consumed: List[String], in: List[String]) =>
      in match {
        case head :: tail =>
          if (oneOf.contains(head)) {
            PathMatchResult.Match(head, consumed.appended(head), tail)
          } else {
            PathMatchResult.Rejected(tail)
          }
        case Nil          => PathMatchResult.NoMatch
      }

  def segment(oneOf: Set[String]): PathMatcher[String] =
    (consumed: List[String], in: List[String]) =>
      in match {
        case head :: tail =>
          if (oneOf.contains(head)) {
            PathMatchResult.Match(head, consumed.appended(head), tail)
          } else {
            PathMatchResult.Rejected(tail)
          }
        case Nil          => PathMatchResult.NoMatch
      }

  def segment(s: String): PathMatcher0 =
    (consumed: List[String], in: List[String]) =>
      in match {
        case head :: tail =>
          if (head == s) {
            PathMatchResult.Match((), consumed.appended(head), tail)
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
