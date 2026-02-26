package frontroute

import frontroute.ops.DirectiveOfOptionOps

trait DirectiveCross {

  implicit def directiveOfOptionSyntax[A](underlying: Directive[Option[A]]): DirectiveOfOptionOps[A] = new DirectiveOfOptionOps[A](underlying)

}
