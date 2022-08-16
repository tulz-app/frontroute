package io.frontroute.internal

import com.raquo.laminar.api.L._
import io.frontroute.DefaultLocationProvider
import io.frontroute.Route
import io.frontroute.RouteLocation
import org.scalajs.dom

import scala.annotation.tailrec
import scala.scalajs.js
import scala.scalajs.js.UndefOr

@js.native
private[internal] trait ElementWithLocationState extends js.Any {

  var ____locationState: js.UndefOr[LocationState]

}

private[frontroute] class RouterStateRef {

  private var states: Map[Route, RoutingState] = Map.empty

  def get(r: Route): Option[RoutingState] = states.get(r)

  def set(r: Route, next: RoutingState): Unit = {
    states = states.updated(r, next)
  }

  def unset(r: Route): Unit = {
    states = states.removed(r)
  }

}

private[frontroute] object LocationState {

  lazy val default: LocationState = {
    var siblingMatched = false
    DefaultLocationProvider.location.foreach { _ => siblingMatched = false }(unsafeWindowOwner)

    val state = new LocationState(
      location = DefaultLocationProvider.location,
      siblingMatched = () => siblingMatched,
      notifyMatched = () => { siblingMatched = true },
      routerState = new RouterStateRef,
    )
    state.start()(unsafeWindowOwner)
    state
  }

  def apply(el: Element): UndefOr[LocationState] =
    el.ref.asInstanceOf[ElementWithLocationState].____locationState

  @tailrec
  def closestOrDefault(node: dom.Node): LocationState = {
    val withState = node.asInstanceOf[ElementWithLocationState]
    if (withState.____locationState.isEmpty) {
      if (node.parentNode != null) {
        closestOrDefault(node.parentNode)
      } else {
        withState.____locationState = default
        default
      }
    } else {
      withState.____locationState.get
    }
  }

  def initIfMissing(node: dom.Node, init: () => LocationState): Unit = {
    val resultWithState = node.asInstanceOf[ElementWithLocationState]
    if (resultWithState.____locationState.isEmpty) {
      resultWithState.____locationState = init()
    }
  }

}

private[frontroute] class LocationState(
  val location: StrictSignal[Option[RouteLocation]],
  val siblingMatched: () => Boolean,
  val notifyMatched: () => Unit,
  val routerState: RouterStateRef,
) {

  private val remainingVar                           = Var(Option.empty[RouteLocation])
  val remaining: StrictSignal[Option[RouteLocation]] = remainingVar.signal

  def setRemaining(remaining: Option[RouteLocation]): Unit = remainingVar.set(remaining)

  private var _childMatched       = false
  val onChildMatched: () => Unit  = () => { _childMatched = true }
  val childMatched: () => Boolean = () => _childMatched

  private var locationSubscription: Subscription = _

  def start()(implicit owner: Owner): Unit = {
    if (locationSubscription == null) {
      locationSubscription = location.changes.foreach { _ => _childMatched = false }
    }
  }

  def kill(): Unit = {
    if (locationSubscription != null) {
      locationSubscription.kill()
      locationSubscription = null
    }
  }

}
