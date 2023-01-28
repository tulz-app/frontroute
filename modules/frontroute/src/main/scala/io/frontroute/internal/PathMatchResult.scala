package io.frontroute.internal

sealed abstract class PathMatchResult[+A] extends Product with Serializable {

  def map[B](f: A => B): PathMatchResult[B] = this match {
    case PathMatchResult.NoMatch                      => PathMatchResult.NoMatch
    case PathMatchResult.Rejected(tail)               => PathMatchResult.Rejected(tail)
    case PathMatchResult.Match(value, consumed, tail) => PathMatchResult.Match(f(value), consumed, tail)
  }

}

object PathMatchResult {

  case object NoMatch extends PathMatchResult[Nothing]
  case class Rejected[T](
    tail: List[String]
  ) extends PathMatchResult[T]
  case class Match[T](
    value: T,
    consumed: List[String],
    tail: List[String]
  ) extends PathMatchResult[T]

}
