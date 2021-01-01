package io.frontroute

import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.matching.Regex
import scala.util.matching.Regex.Match
import app.tulz.tuplez.Composition

abstract class PathMatcher[T](val description: String) {
  self =>

  def apply(path: List[String]): Either[(String, List[String]), (T, List[String])]

  def map[V](f: T => V): PathMatcher[V] = new PathMatcher[V](self.description) {
    override def apply(in: List[String]): Either[(String, List[String]), (V, List[String])] =
      self(in).map { case (t, out) =>
        f(t) -> out
      }
  }

  def flatMap[V](description: String)(f: T => PathMatcher[V]): PathMatcher[V] = new PathMatcher[V](description) {
    override def apply(path: List[String]): Either[(String, List[String]), (V, List[String])] =
      self(path).flatMap { case (t, out) =>
        f(t).apply(out)
      }
  }

  def filter(description: String)(f: T => Boolean): PathMatcher[T] = this.flatMap(description) { t =>
    if (f(t)) {
      PathMatcher.provide(t)
    } else {
      PathMatcher.fail("filter failed")
    }
  }

  def collect[V](description: String)(f: PartialFunction[T, V]): PathMatcher[V] = this.flatMap(description) { t =>
    if (f.isDefinedAt(t)) {
      PathMatcher.provide(f(t))
    } else {
      PathMatcher.fail("collect failed")
    }
  }

  def withFilter(description: String)(f: T => Boolean): PathMatcher[T] = this.filter(description)(f)

  def /[V](other: PathMatcher[V])(implicit compose: Composition[T, V]): PathMatcher[compose.Composed] =
    self.flatMap(s"${self.description}/${other.description}") { t1 =>
      other.map { v =>
        compose.compose(t1, v)
      }
    }

  def as[O](f: T => O): PathMatcher[O] = self.map(f)

  def void: PathMatcher[Unit] = this.map(_ => ())

  def unary_! : PathMatcher[Unit] = new PathMatcher[Unit](s"!${self.description}") {
    override def apply(path: List[String]): Either[(String, List[String]), (Unit, List[String])] =
      self(path) match {
        case Right((_, rest)) => Left("not !matched" -> rest)
        case Left((_, rest))  => Right(() -> rest)
      }
  }

  override def toString: String = description

}

object PathMatcher {

  val unit: PathMatcher[Unit] = new PathMatcher[Unit]("unit") {
    override def apply(path: List[String]): Either[(String, List[String]), (Unit, List[String])] =
      Right(() -> path)
  }

  def provide[V](v: V): PathMatcher[V] = unit.map(_ => v)

  def fail[T](msg: String): PathMatcher[T] = new PathMatcher[T]("fail") {
    override def apply(path: List[String]): Either[(String, List[String]), (T, List[String])] =
      Left(msg -> path)
  }

}

trait PathMatchers {

  def segment: PathMatcher[String] = new PathMatcher[String]("segment") {

    override def apply(path: List[String]): Either[(String, List[String]), (String, List[String])] =
      path match {
        case head :: tail => Right(head -> tail)
        case Nil          => Left(s"unexpected end of path" -> Nil)
      }

  }

  def segment(s: String): PathMatcher0 = segment.filter(s)(_ == s).void

  def regex(r: Regex): PathMatcher[Match] =
    segment
      .map(s => r.findFirstMatchIn(s))
      .collect(s"regex($r)") { case Some(m) => m }

  def fromTry[V](t: Try[V]): PathMatcher[V] = new PathMatcher[V]("fromTry") {
    override def apply(path: List[String]): Either[(String, List[String]), (V, List[String])] =
      t match {
        case Success(value) =>
          Right(value -> path)
        case Failure(exception) =>
          Left(exception.getMessage -> path)
      }
  }

  def tryParse[V](t: => V): PathMatcher[V] = fromTry(Try(t))

  def long: PathMatcher[Long] = segment.flatMap("long") { matched =>
    tryParse(matched.toLong)
  }

  def double: PathMatcher[Double] = segment.flatMap("double") { matched =>
    tryParse(matched.toDouble)
  }

  implicit def stringToSegment(s: String): PathMatcher[Unit] = segment(s)

}
