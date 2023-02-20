package frontroute
package ops

class DirectiveOfOptionOps[A](underlying: Directive[Option[A]]) {

  @inline def mapOption[R](f: A => R): Directive[Option[R]] = underlying.map(_.map(f))

  @inline def default(v: => A): Directive[A] = underlying.map(_.getOrElse(v))

  @inline def collectOption[R](f: PartialFunction[A, R]): Directive[Option[R]] = underlying.map(_.collect(f))

}
