package io.frontroute

import com.raquo.laminar.api.L._

trait ToComplete {
  def get: Signal[HtmlElement]
}

object ToComplete {

  implicit def elementToComplete(value: HtmlElement): ToComplete = new ToComplete {
    override val get: Signal[HtmlElement] = Val(value)
  }

  implicit def signalToComplete(value: Signal[HtmlElement]): ToComplete = new ToComplete {
    override val get: Signal[HtmlElement] = value
  }

}
