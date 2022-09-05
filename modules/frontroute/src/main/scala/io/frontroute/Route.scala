package io.frontroute

import com.raquo.laminar.api.L._
import com.raquo.laminar.modifiers.Binder
import com.raquo.laminar.nodes.ReactiveElement
import io.frontroute.internal.LocationState
import io.frontroute.internal.RoutingState
import io.frontroute.internal.RouterStateRef
import io.frontroute.internal.SignalToStream

trait Route extends ((Location, RoutingState, RoutingState) => Signal[RouteResult]) with Mod[HtmlElement] {

  import Route._

  private val currentRender = Var(Option.empty[Element])

  private def bind: Binder[HtmlElement] = {
    Binder { el =>
      ReactiveElement.bindCallback(el) { ctx =>
        val locationState = LocationState.closestOrDefault(el.ref)
        val childStateRef = new RouterStateRef

        // the returned subscription will be managed by the ctx.owner
        val _ = SignalToStream(locationState.location)
          .delay(0)
          .collect { case Some(currentUnmatched) => currentUnmatched }
          .flatMap { currentUnmatched =>
            SignalToStream(
              this.apply(
                currentUnmatched.copy(otherMatched = locationState.siblingMatched()),
                locationState.routerState.get(this).fold(RoutingState.empty)(_.resetPath),
                RoutingState.empty
              )
            )
          }
          .flatMap {
            case RouteResult.Matched(nextState, location, createResult) =>
              if (
                !locationState.routerState.get(this).contains(nextState) ||
                currentRender.now().isEmpty
              ) {
                SignalToStream(
                  createResult().map(RouteEvent.NextRender(nextState, location, _))
                )
              } else {
                EventStream.fromValue(RouteEvent.SameRender(nextState, location))
              }

            case RouteResult.Rejected =>
              EventStream.fromValue(RouteEvent.NoRender)
          }
          .foreach {
            case RouteEvent.NextRender(nextState, remaining, render) =>
              locationState.routerState.set(this, nextState)

              locationState.setRemaining(Some(remaining))
              locationState.notifyMatched()

              if (render != null) {
                LocationState.initIfMissing(
                  render.ref,
                  () =>
                    new LocationState(
                      locationState.remaining,
                      locationState.childMatched,
                      locationState.onChildMatched,
                      childStateRef,
                    )
                )

                val amendedRender = render.amend(
                  onMountUnmountCallback(
                    ctx => LocationState(ctx.thisNode).foreach(_.start()(ctx.owner)),
                    el => LocationState(el).foreach(_.kill())
                  ),
                )
                currentRender.set(Some(amendedRender))
              } else {
                currentRender.set(None) // route matched but rendered a null
              }

            case RouteEvent.SameRender(nextState, remaining) =>
              locationState.routerState.set(this, nextState)

              locationState.setRemaining(Some(remaining))
              locationState.notifyMatched()

            case RouteEvent.NoRender =>
              locationState.routerState.unset(this)
              currentRender.set(None)
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
      render: Element
    ) extends RouteEvent

    case class SameRender(
      nextState: RoutingState,
      remaining: Location
    ) extends RouteEvent

    case object NoRender extends RouteEvent

  }

}
