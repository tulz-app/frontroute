package io.frontroute

import com.raquo.laminar.api.L
import com.raquo.laminar.api.L._

trait ToComplete[A] {
  def get: Signal[A]
}

object ToComplete {

  implicit def liftToComplete[A](value: A): ToComplete[A] = new ToComplete[A] {
    override def get: L.Signal[A] = Val(value)
  }

  implicit def signalToComplete[A](value: Signal[A]): ToComplete[A] = new ToComplete[A] {
    override def get: L.Signal[A] = value
  }

}
