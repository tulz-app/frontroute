package io.frontroute.internal

import com.raquo.laminar.api.L._
import io.frontroute.Location
import io.frontroute.LocationProvider
import io.frontroute.Route
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

  lazy val default: LocationState = withLocationProvider(LocationProvider.windowLocationProvider)(unsafeWindowOwner)

  def withLocationProvider(lp: LocationProvider)(implicit owner: Owner): LocationState = {
    var siblingMatched = false
    lp.current.foreach { _ =>
      siblingMatched = false
    }

    val state = new LocationState(
      location = lp.current,
      isSiblingMatched = () => {
        siblingMatched
      },
      resetSiblingMatched = () => {
        siblingMatched = false
      },
      notifySiblingMatched = () => {
        siblingMatched = true
      },
      routerState = new RouterStateRef,
    )

    lp.start()
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
//        withState.____locationState = default
        default
      }
    } else {
      withState.____locationState.get
    }
  }

  @tailrec
  def closest(node: dom.Node): Option[LocationState] = {
    val withState = node.asInstanceOf[ElementWithLocationState]
    if (withState.____locationState.isEmpty) {
      if (node.parentNode != null) {
        closest(node.parentNode)
      } else {
        Option.empty
      }
    } else {
      Some(withState.____locationState.get)
    }
  }

  def initIfMissing(node: dom.Node, init: () => LocationState): LocationState = {
    val resultWithState = node.asInstanceOf[ElementWithLocationState]
    if (resultWithState.____locationState.isEmpty) {
      resultWithState.____locationState = init()
    }
    resultWithState.____locationState.get
  }

}

private[frontroute] class LocationState(
  val location: Signal[Option[Location]],
  val isSiblingMatched: () => Boolean,
  val resetSiblingMatched: () => Unit,
  val notifySiblingMatched: () => Unit,
  val routerState: RouterStateRef,
) {

  private val remainingVar                      = Var(Option.empty[Location])
  val remaining: StrictSignal[Option[Location]] = remainingVar.signal

  def setRemaining(remaining: Option[Location]): Unit = remainingVar.set(remaining)

  private val consumedVar                  = Var(List.empty[String])
  val consumed: StrictSignal[List[String]] = consumedVar.signal

  def setConsumed(consumed: List[String]): Unit = {
    consumedVar.set(consumed)
  }

  private var _childMatched          = false
  val notifyChildMatched: () => Unit = () => { _childMatched = true }
  val resetChildMatched: () => Unit  = () => { _childMatched = false }
  val isChildMatched: () => Boolean  = () => _childMatched

}
