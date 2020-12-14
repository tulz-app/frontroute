package app.tulz.routing

import app.tulz.util.Tuple

import scala.language.implicitConversions
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.matching.Regex
import scala.util.matching.Regex.Match
import app.tulz.util.TupleComposition.Composition

abstract class PathMatcher[T](val description: String)(implicit val tuple: Tuple[T]) {
  self =>

  def apply(path: List[String]): Either[(String, List[String]), (T, List[String])]

  def tmap[V: Tuple](f: T => V): PathMatcher[V] = new PathMatcher[V](self.description) {
    override def apply(in: List[String]): Either[(String, List[String]), (V, List[String])] =
      self(in).map { case (t, out) =>
        f(t) -> out
      }
  }

  def tflatMap[V: Tuple](description: String)(f: T => PathMatcher[V]): PathMatcher[V] = new PathMatcher[V](description) {
    override def apply(path: List[String]): Either[(String, List[String]), (V, List[String])] =
      self(path).flatMap { case (t, out) =>
        f(t).apply(out)
      }
  }

  def tfilter(description: String)(f: T => Boolean): PathMatcher[T] = this.tflatMap(description) { t =>
    if (f(t)) {
      PathMatcher.tprovide(t)
    } else {
      PathMatcher.fail("filter failed")
    }
  }

  def tcollect[V: Tuple](description: String)(f: PartialFunction[T, V]): PathMatcher[V] = this.tflatMap(description) { t =>
    if (f.isDefinedAt(t)) {
      PathMatcher.tprovide(f(t))
    } else {
      PathMatcher.fail("collect failed")
    }
  }

  def withFilter(description: String)(f: T => Boolean): PathMatcher[T] = this.tfilter(description)(f)

  def /[V](other: PathMatcher[V])(implicit compose: Composition[T, V]): PathMatcher[compose.C] =
    self.tflatMap(s"${self.description}/${other.description}") { t1 =>
      other.tmap { v =>
        compose.gc(t1, v)
      }(Tuple.yes)
    }(Tuple.yes)

  def as[O: Tuple](f: T => O): PathMatcher[O] = self.tmap(f)

  def void: PathMatcher[Unit] = this.tmap(_ => ())

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

  def tprovide[V: Tuple](v: V): PathMatcher[V] = unit.tmap(_ => v)

  def provide[V](v: V): PathMatcher[Tuple1[V]] = tprovide(Tuple1(v))

  def fail[T: Tuple](msg: String): PathMatcher[T] = new PathMatcher[T]("fail") {
    override def apply(path: List[String]): Either[(String, List[String]), (T, List[String])] =
      Left(msg -> path)
  }

}

object PathMatchers extends PathMatchers

trait PathMatchers {

  implicit class PathMatcher1Ops[T](matcher: PathMatcher1[T]) {

    def map[R](f: T => R): PathMatcher1[R] = matcher.tmap { case Tuple1(e) => Tuple1(f(e)) }

    def collect[R](description: String)(f: PartialFunction[T, R]): PathMatcher1[R] = matcher.tcollect(description) { case Tuple1(e) if f.isDefinedAt(e) => Tuple1(f(e)) }

    def flatMap[R](description: String)(f: T => PathMatcher1[R]): PathMatcher1[R] =
      matcher.tflatMap(description) { case Tuple1(e) => f(e) }
  }

  def segment: PathMatcher1[String] = new PathMatcher1[String]("segment") {

    override def apply(path: List[String]): Either[(String, List[String]), (Tuple1[String], List[String])] =
      path match {
        case head :: tail => Right(Tuple1(head) -> tail)
        case Nil          => Left(s"unexpected end of path" -> Nil)
      }

  }

  def segment(s: String): PathMatcher0 = segment.tfilter(s)(t => t._1 == s).void

  def regex(r: Regex): PathMatcher1[Match] =
    segment
      .tmap(s => Tuple1(r.findFirstMatchIn(s._1)))
      .tcollect(s"regex($r)") { case Tuple1(Some(m)) =>
        Tuple1(m)
      }

  def fromTry[V](t: Try[V]): PathMatcher1[V] = new PathMatcher1[V]("fromTry") {
    override def apply(path: List[String]): Either[(String, List[String]), (Tuple1[V], List[String])] =
      t match {
        case Success(value) =>
          Right(Tuple1(value) -> path)
        case Failure(exception) =>
          Left(exception.getMessage -> path)
      }
  }

  def tryParse[V](t: => V): PathMatcher1[V] = fromTry(Try(t))

  def long: PathMatcher1[Long] = segment.tflatMap("long") { matched =>
    tryParse(matched._1.toLong)
  }

  def double: PathMatcher1[Double] = segment.tflatMap("double") { matched =>
    tryParse(matched._1.toDouble)
  }

  implicit def stringToSegment(s: String): PathMatcher[Unit] = segment(s)

}
