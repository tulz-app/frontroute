package io.frontroute.internal

import com.raquo.laminar.api.L._
import io.frontroute.RouteLocation

import scala.scalajs.js

@js.native
private[frontroute] trait ElementWithLocationState extends js.Any {

  var ____locationState: js.UndefOr[LocationState]

}

private[frontroute] class LocationState {

  private val locationVar  = Var(Option.empty[RouteLocation])
  private val remainingVar = Var(Option.empty[RouteLocation])

  val locationObserver: Observer[Option[RouteLocation]] = locationVar.writer
  val location: StrictSignal[Option[RouteLocation]]     = locationVar.signal

  def emitRemaining(remaining: Option[RouteLocation]): Unit =
    remainingVar.set(remaining)

  def subscribeToRemaining(sink: Observer[Option[RouteLocation]])(implicit owner: Owner): Subscription =
    remainingVar.signal.changes.foreach(sink.toObserver.onNext)

}
