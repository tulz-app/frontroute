package io.frontroute.domext

import scala.scalajs.js

@js.native
private[frontroute] trait WindowWithScrollXY extends js.Object {

  val scrollX: js.UndefOr[Double] = js.native
  val scrollY: js.UndefOr[Double] = js.native

}
