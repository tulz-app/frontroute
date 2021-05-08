package io.frontroute
package ops

class DirectiveOfOptionOps[A](underlying: Directive[Option[A]]) {

  @inline def mapOption[R](f: A => R): Directive[Option[R]] = underlying.map(_.map(f))

}
