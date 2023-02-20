package io.frontroute

import com.raquo.laminar.api.L._
import com.raquo.laminar.modifiers.Binder
import com.raquo.laminar.nodes.ReactiveElement
import io.frontroute.internal.LocationState
import io.frontroute.internal.RoutingState
import io.frontroute.internal.RouterStateRef

trait Route extends ((Location, RoutingState, RoutingState) => RouteResult) with Mod[HtmlElement] {

  import Route._

  private val currentRender      = Var(Option.empty[HtmlElement])
  private val currentRenderState = Var(Option.empty[LocationState])

  private def bind: Binder[HtmlElement] = {
    Binder { el =>
      ReactiveElement.bindCallback(el) { ctx =>
        val locationState = LocationState.closestOrFail(el.ref)
        val childStateRef = new RouterStateRef

        // the returned subscription will be managed by the ctx.owner
        val _ = locationState.location
          .foreach {
            case Some(currentUnmatched) =>
              val renderResult = this.apply(
                currentUnmatched.copy(otherMatched = locationState.isSiblingMatched()),
                locationState.routerState.get(this).fold(RoutingState.empty)(_.resetPath),
                RoutingState.empty.withConsumed(locationState.consumed.now())
              ) match {
                case RouteResult.Matched(nextState, location, consumed, createResult) =>
                  locationState.resetChildMatched()
                  locationState.notifySiblingMatched()
                  if (
                    !locationState.routerState.get(this).contains(nextState) ||
                    currentRender.now().isEmpty
                  ) {
                    RouteEvent.NextRender(nextState, location, consumed, createResult())
                  } else {
                    RouteEvent.SameRender(nextState, location, consumed)
                  }
                case RouteResult.RunEffect(nextState, location, consumed, run)        =>
                  locationState.notifySiblingMatched()
                  if (!locationState.routerState.get(this).contains(nextState)) {
                    run()
                    RouteEvent.SameRender(nextState, location, consumed)
                  } else {
                    RouteEvent.SameRender(nextState, location, consumed)
                  }

                case RouteResult.Rejected =>
                  RouteEvent.NoRender
              }
              renderResult match {
                case RouteEvent.NextRender(nextState, remaining, consumed, render) =>
                  locationState.routerState.set(this, nextState)

                  locationState.setRemaining(Some(remaining))
                  val childState = new LocationState(
                    location = locationState.remaining,
                    isSiblingMatched = locationState.isChildMatched,
                    resetSiblingMatched = locationState.resetChildMatched,
                    notifySiblingMatched = locationState.notifyChildMatched,
                    routerState = childStateRef,
                  )
                  LocationState.init(render.ref, childState)
                  childState.setConsumed(consumed)

                  render.ref.dataset.addOne("frPath" -> consumed.mkString("/", "/", ""))
                  currentRender.set(Some(render))
                  currentRenderState.set(Some(childState))
                case RouteEvent.SameRender(nextState, remaining, consumed)         =>
                  locationState.routerState.set(this, nextState)
                  currentRenderState.now().foreach { childState =>
                    childState.setConsumed(consumed)
                  }
                  currentRender.now().foreach { render =>
                    render.ref.dataset.addOne("frPath" -> consumed.mkString("/", "/", ""))
                  }

                  locationState.setRemaining(Some(remaining))
                case RouteEvent.NoRender                                           =>
                  locationState.routerState.unset(this)
                  currentRender.set(None)
                  currentRenderState.set(None)
              }
            case None                   =>
//              locationState.routerState.unset(this)
//              currentRender.set(None)
          }(ctx.owner)
      }
    }
  }

  override def apply(element: HtmlElement): Unit = {
    element.amend(
      child.maybe <-- currentRender.signal,
      bind,
    )
  }

}

object Route {

  implicit def toDirective[L](route: Route): Directive[L] = Directive[L](_ => route)

  sealed private[frontroute] trait RouteEvent extends Product with Serializable

  private[frontroute] object RouteEvent {

    case class NextRender(
      nextState: RoutingState,
      remaining: Location,
      nextConsumed: List[String],
      render: HtmlElement
    ) extends RouteEvent

    case class SameRender(
      nextState: RoutingState,
      remaining: Location,
      nextConsumed: List[String],
    ) extends RouteEvent

    case object NoRender extends RouteEvent

  }

}
