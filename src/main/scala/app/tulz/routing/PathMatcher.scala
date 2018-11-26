package app.tulz.routing

import scala.language.implicitConversions
import scala.util.Try
import scala.util.matching.Regex
import scala.util.matching.Regex.Match
import app.tulz.routing.TupleComposition.Composition

trait PathMatcher[T] {
  self =>

  def apply(in: List[String]): Either[(String, List[String]), (T, List[String])]

  def map[V](f: T => V): PathMatcher[V] =
    (path: List[String]) =>
      self(path).map {
        case (t, out) => f(t) -> out
    }

  def flatMap[V](f: T => PathMatcher[V]): PathMatcher[V] =
    (path: List[String]) =>
      self(path).flatMap {
        case (t, out) => f(t).apply(out)
    }

  def filter(f: T => Boolean): PathMatcher[T] = this.flatMap { t =>
    if (f(t)) {
      PathMatchers.provide(t)
    } else {
      PathMatchers.fail("filter failed")
    }
  }

  def collect[V](f: PartialFunction[T, V]): PathMatcher[V] = this.flatMap { t =>
    if (f.isDefinedAt(t)) {
      PathMatchers.provide(f(t))
    } else {
      PathMatchers.fail("collect failed")
    }
  }

  def withFilter(f: T => Boolean): PathMatcher[T] = this.filter(f)

  def /[V](other: PathMatcher[V])(implicit compose: Composition[T, V]): PathMatcher[compose.C] =
    for {
      t1 <- self
      v  <- other
    } yield compose.gc(t1, v)

  def |[V >: T](other: PathMatcher[V]): PathMatcher[V] =
    (path: List[String]) =>
      self(path) match {
        case r @ Right(_) => r
        case Left(myMsg) =>
          other(path) match {
            case r @ Right(_) => r
            case Left( (theirMsg, theirRest)) =>
              Left(Seq(myMsg, theirMsg).mkString("; ") -> theirRest)
          }
    }

  def caseClass[O](f: T => O): PathMatcher[O] = self.map(f)

  def void: PathMatcher[Unit] = this.map(_ => ())

  def unary_!(): PathMatcher[Unit] =
    (path: List[String]) =>
      self(path) match {
        case Right( (_, rest) ) => Left("not !matched" -> rest)
        case Left( (_, rest) ) => Right( () -> rest )
      }

}

object PathMatchers extends PathMatchers

trait PathMatchers {

  val unit: PathMatcher[Unit] = (path: List[String]) => Right(() -> path)

  def provide[V](v: V): PathMatcher[V] = unit.map(_ => v)

  def fail[T](msg: String): PathMatcher[T] = (path: List[String]) => Left(msg -> path)

  def segment: PathMatcher[String] = {
    case head :: tail => Right[(String, List[String]), (String, List[String])](head -> tail)
    case Nil          => Left(s"unexpected end of path" -> Nil)
  }

  def segment(s: String): PathMatcher[Unit] = segment.filter(_ == s).void

  def regex(r: Regex): PathMatcher[Match] =
    segment
      .map(s => r.findFirstMatchIn(s))
      .collect {
        case Some(m) => m
      }

  def fromOption[V](o: Option[V]): PathMatcher[V] =
    (path: List[String]) =>
      o match {
        case Some(v) => Right(v -> path)
        case None    => Left("empty option" -> path)
    }

  def fromTry[V](t: Try[V]): PathMatcher[V] = fromOption(t.toOption)

  def tryParse[V](t: => V): PathMatcher[V] = fromTry(Try(t))

  def long: PathMatcher[Long] = segment.flatMap { matched =>
    tryParse(matched.toLong)
  }

  def double: PathMatcher[Double] = segment.flatMap { matched =>
    tryParse(matched.toDouble)
  }

  implicit def stringToSegment(s: String): PathMatcher[Unit] = segment(s)

}
