package io.frontroute.internal

import com.raquo.laminar.api.L._
import io.frontroute.RouteLocation

import scala.scalajs.js

@js.native
private[frontroute] trait ElementWithLocationState extends js.Any {

  var ____locationState: js.UndefOr[LocationState]

}

private[frontroute] class LocationState(
  $providedLocation: Signal[Option[RouteLocation]],
  $siblingMatched: StrictSignal[Boolean],
  matchedObserver: Observer[Unit],
  owner: Owner
) {

  private val locationVar                           = Var(Option.empty[RouteLocation])
  val location: StrictSignal[Option[RouteLocation]] = locationVar.signal

  private val remainingVar                           = Var(Option.empty[RouteLocation])
  val remaining: StrictSignal[Option[RouteLocation]] = remainingVar.signal

  private val childMatchedVar              = Var(false)
  val onChildMatched: Observer[Unit]       = childMatchedVar.writer.contramap(_ => true)
  val $childMatched: StrictSignal[Boolean] = childMatchedVar.signal

  private val locationSubscription = $providedLocation.changes.foreach { l =>
    locationVar.set(l)
    childMatchedVar.set(false)
  }(owner)

  def siblingMatched: Boolean = $siblingMatched.now()

  def notifyMatched(): Unit = {
    matchedObserver.onNext(())
  }

  def emitRemaining(remaining: Option[RouteLocation]): Unit =
    remainingVar.set(remaining)

  def kill(): Unit = {
    locationSubscription.kill()
  }

}
