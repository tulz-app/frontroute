package io.frontroute

import com.raquo.airstream.state.Var

import scala.annotation.tailrec
import scala.scalajs.js

private[frontroute] object RoutingState {

  val empty: RoutingState = RoutingState(
    path = List.empty,
    data = Map.empty,
    persistent = Map.empty
  )

  def withPersistentData(
    persistentData: Map[List[String], Any]
  ): RoutingState = RoutingState(
    path = List.empty,
    data = Map.empty,
    persistent = persistentData
  )

}

final private[frontroute] case class RoutingState(
  path: List[String],
  data: Map[List[String], Any],
  persistent: Map[List[String], Any]
) {

  def resetPath: RoutingState = this.copy(path = List.empty)

  def path(c: String): RoutingState = this.copy(path = c :: path)

  def enterDisjunction(): RoutingState = this.copy(path = "(" :: path)

  def leaveDisjunction(): RoutingState = {
    @tailrec
    def leave(path: List[String], data: Map[List[String], Any], value: Option[Any]): RoutingState = {
      path match {
        case Nil => this

        case "(" :: tail =>
          val newPath = "(.|.)" :: tail
          this.copy(path = newPath, data = data + (newPath -> value))

        case other => leave(other.tail, data - other, value)

      }
    }
    leave(path, data, getValue(path))
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

  def getPersistentValue[T](at: List[String]): Option[T] = {
    persistent.get(at).map(_.asInstanceOf[T])
  }

  def setPersistentValue[T](nv: T): RoutingState = {
    //noinspection ComparingUnrelatedTypes
    if (nv != ((): Unit)) {
      val v = {
        if (js.isUndefined(nv)) {
          "∅"
        } else {
          nv
        }
      }
      this.copy(persistent = persistent + (path -> v))
    } else {
      this
    }
  }

  def unsetPersistentValue[T](): RoutingState = {
    this.copy(persistent = persistent - path)
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
