package io.frontroute.ops

import com.raquo.laminar.api.L._
import io.frontroute.Directive

class DirectiveOfOptionOps(underlying: Directive[Option[Element]]) {

  @inline def mapOption[R](f: Element => R): Directive[Option[R]] = underlying.map(_.map(f))

}
