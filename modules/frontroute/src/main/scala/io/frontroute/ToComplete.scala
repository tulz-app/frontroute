package io.frontroute

import com.raquo.laminar.api.L._

trait ToComplete {
  def get: Signal[Element]
}

object ToComplete {

  implicit def elementToComplete(value: Element): ToComplete = new ToComplete {
    override val get: Signal[Element] = Val(value)
  }

  implicit def signalToComplete(value: Signal[Element]): ToComplete = new ToComplete {
    override val get: Signal[Element] = value
  }

}
