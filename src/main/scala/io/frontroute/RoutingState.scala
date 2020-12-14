package io.frontroute

import com.raquo.airstream.signal.Var

import scala.annotation.tailrec
import scala.scalajs.js

object RoutingState {

  val empty: RoutingState = RoutingState(
    path = List.empty,
    data = Map.empty
  )

}

final private[frontroute] case class RoutingState(
  path: List[String],
  data: Map[List[String], Any]
) {

  def resetPath: RoutingState = this.copy(path = List.empty)

  def path(c: String): RoutingState = this.copy(path = c :: path)

  def enterDisjunction(): RoutingState = this.copy(path = "(" :: path)

  def leaveDisjunction(): RoutingState = {
    @tailrec
    def leave(path: List[String], data: Map[List[String], Any]): RoutingState =
      path match {
        case Nil         => this
        case "(" :: tail => this.copy(path = "(...|...)" :: tail, data = data)
        case other       => leave(other.tail, data - other)
      }
    leave(path, data)
  }

  def getValue[T](at: List[String]): Option[T] = {
    data.get(at).map(_.asInstanceOf[T])
  }

  def setValue[T](nv: T): RoutingState = {
    //noinspection ComparingUnrelatedTypes
    if (nv != ((): Unit)) {
      val v = {
        if (js.isUndefined(nv)) {
          "∅"
        } else {
          nv
        }
      }
      this.copy(data = data + (path -> v))
    } else {
      this
    }
  }

  def unsetValue[T](): RoutingState = {
    this.copy(data = data - path)
  }

  private def showPath(path: List[String]) = path.reverse.mkString(" ")

  override def toString: String = {
    s"""matched: ${showPath(path)}\n${data
      .map {
        case (key, value: Var[_]) =>
          s"  ${key.reverse.mkString(" ")} -> $value (${value.signal.now()})"
        case (key, value) =>
          s"  ${key.reverse.mkString(" ")} -> $value"
      }
      .mkString("\n")}"""
  }

}
