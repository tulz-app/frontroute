package io.frontroute.internal

import com.raquo.laminar.api.L._
import io.frontroute.Route
import io.frontroute.RouteLocation
import org.scalajs.dom

import scala.annotation.tailrec
import scala.scalajs.js

private[frontroute] object ElementWithLocationState {

  def getOrInit(node: dom.Node, init: () => LocationState): LocationState = {
    val resultWithState = node.asInstanceOf[ElementWithLocationState]
    if (resultWithState.____locationState.isEmpty) {
      resultWithState.____locationState = init()
    }
    resultWithState.____locationState.get
  }

  @tailrec
  def getClosestOrInit(node: dom.Node, init: () => LocationState): LocationState = {
    val withState = node.asInstanceOf[ElementWithLocationState]
    if (withState.____locationState.isEmpty) {
      if (node.parentNode != null) {
        getClosestOrInit(node.parentNode, init)
      } else {
        val initialized = init()
        withState.____locationState = initialized
        initialized
      }
    } else {
      withState.____locationState.get
    }
  }

  @tailrec
  def getClosest(node: dom.Node): Option[LocationState] = {
    val withState = node.asInstanceOf[ElementWithLocationState]
    if (withState.____locationState.isEmpty) {
      if (node.parentNode != null) {
        getClosest(node.parentNode)
      } else {
        None
      }
    } else {
      Some(withState.____locationState.get)
    }
  }

}

@js.native
private[frontroute] trait ElementWithLocationState extends js.Any {

  var ____locationState: js.UndefOr[LocationState]

}

private[frontroute] class RoutingStateRef {

  private var states: Map[Route, RoutingState] = Map.empty

  def get(r: Route): Option[RoutingState] = states.get(r)

  def set(r: Route, next: RoutingState): Unit = {
    states = states.updated(r, next)
  }

  def unset(r: Route): Unit = {
    states = states.removed(r)
  }

}

private[frontroute] class LocationState private (
  $providedLocation: StrictSignal[Option[RouteLocation]],
  $siblingMatched: StrictSignal[Boolean],
  matchedObserver: Observer[Unit],
  val currentState: RoutingStateRef,
  owner: Owner
) {

  private val locationVar                           = Var(Option.empty[RouteLocation])
  val location: StrictSignal[Option[RouteLocation]] = locationVar.signal

  private val remainingVar                           = Var(Option.empty[RouteLocation])
  val remaining: StrictSignal[Option[RouteLocation]] = remainingVar.signal

  private val childMatchedVar              = Var(false)
  val onChildMatched: Observer[Unit]       = childMatchedVar.writer.contramap(_ => true)
  val $childMatched: StrictSignal[Boolean] = childMatchedVar.signal

  private var locationSubscription: Subscription = _

  def siblingMatched: Boolean = $siblingMatched.now()

  def notifyMatched(): Unit = {
    matchedObserver.onNext(())
  }

  def emitRemaining(remaining: Option[RouteLocation]): Unit =
    remainingVar.set(remaining)

  def start(): Unit = {
    if (locationSubscription == null) {
      locationSubscription = $providedLocation.changes.foreach { l =>
        locationVar.set(l)
        childMatchedVar.set(false)
      }(owner)
    }
  }

  def kill(): Unit = {
    if (locationSubscription != null) {
      locationSubscription.kill()
      locationSubscription = null
    }
  }

}

object LocationState {

  def apply(
    $providedLocation: StrictSignal[Option[RouteLocation]],
    $siblingMatched: StrictSignal[Boolean],
    matchedObserver: Observer[Unit],
    currentState: RoutingStateRef,
    owner: Owner
  ): LocationState = {
    val state = new LocationState(
      $providedLocation,
      $siblingMatched,
      matchedObserver,
      currentState,
      owner
    )
    state.start()
    state
  }

}
