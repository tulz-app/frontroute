package io.frontroute.internal

import com.raquo.airstream.state.Var
import scala.scalajs.js

private[frontroute] object RoutingState {

  val empty: RoutingState = new RoutingState(
    consumed = List.empty,
    path = RoutingPath.initial,
    data = Map.empty
  )

}

final private[frontroute] class RoutingState private (
  val consumed: List[String],
  val path: RoutingPath,
  val data: Map[RoutingPath.Key, Any]
) {

  def copy(
    consumed: List[String] = consumed,
    path: RoutingPath = path,
    data: Map[RoutingPath.Key, Any] = data
  ): RoutingState = new RoutingState(
    consumed = consumed,
    path = path,
    data = data
  )

  def withConsumed(consumed: List[String]): RoutingState = this.copy(consumed = consumed)

  def resetPath: RoutingState = this.copy(path = RoutingPath.empty)

  def enter: RoutingState = this.copy(path = path.enter())

  def enterAndSet[T](nv: T): RoutingState = {
    val newPath = path.enter()
    val newData = updateData(data, newPath.key, nv)
    this.copy(
      path = newPath,
      data = newData
    )
  }

  def enterConcat(index: Int): RoutingState = this.copy(path = path.enterConcat(index))

  def enterConjunction: RoutingState = this.copy(path = path.enterConjunction())

  def enterDisjunction: RoutingState = this.copy(path = path.enterDisjunction())

  def leaveDisjunction: RoutingState = {
    val value = getValue(path.key)
    path.leaveDisjunction() match {
      case Some((newPath, dropped)) =>
        this.copy(
          path = newPath,
          data = data.removedAll(dropped).updated(newPath.key, value)
        )
      case None                     =>
        this
    }
  }

  def getValue[T](at: RoutingPath.Key): Option[T] = {
    data.get(at).map(_.asInstanceOf[T])
  }

  private def updateData[T](data: Map[RoutingPath.Key, Any], path: RoutingPath.Key, nv: T) =
    // noinspection ComparingUnrelatedTypes
    if (nv != ((): Unit)) {
      val v = {
        if (js.isUndefined(nv)) {
          "∅"
        } else {
          nv
        }
      }
      data.updated(path, v)
    } else {
      data
    }

  def setValue[T](nv: T): RoutingState = {
    this.copy(data = updateData(data, path.key, nv))
  }

  def unsetValue(): RoutingState = {
    this.copy(data = data.removed(path.key))
  }

  override def toString: String = {
    s"""state: consumed: '${consumed.mkString("/", "/", "")}' path: '$path', data: '${data
        .map {
          case (key, value: Var[_]) =>
            s"  $key -> $value (${value.signal.now()})"
          case (key, value)         =>
            s"  $key -> $value"
        }
        .mkString("\n")}'"""
  }

  override def equals(other: Any): Boolean = other match {
    case that: RoutingState => path == that.path && data == that.data
    case _                  => false
  }

  override def hashCode(): Int = {
    val state = Seq(path, data)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

}
