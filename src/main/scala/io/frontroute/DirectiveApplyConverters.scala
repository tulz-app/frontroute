package io.frontroute

import app.tulz.tuplez._
import Types.Route

trait DirectiveApplyConverters extends ApplyConverters[Route] {

  implicit def addDirectiveApply[L](directive: Directive[L])(implicit hac: ApplyConverter[L, Route]): hac.In => Route =
    subRoute =>
      (ctx, previous, state) => {
        val result = directive.tapply(hac(subRoute))(ctx, previous, state)
        result
      }

  implicit def addNullaryDirectiveApply(directive: Directive0): Route => Route =
    subRoute =>
      (ctx, previous, state) => {
        val result = directive.tapply(_ => subRoute)(ctx, previous, state)
        result
      }

}
