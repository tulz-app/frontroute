package frontroute
package ops

// these methods are defined as extension methods in scala-3
// when changing this trait, make sure to update the `DirectiveCross.scala` in `src/scala-3`
class DirectiveOfOptionOps[A](underlying: Directive[Option[A]]) {

  @inline def mapOption[R](f: A => R): Directive[Option[R]] = underlying.map(_.map(f))

  @inline def default(v: => A): Directive[A] = underlying.map(_.getOrElse(v))

  @inline def collectOption[R](f: PartialFunction[A, R]): Directive[Option[R]] = underlying.map(_.collect(f))

}
