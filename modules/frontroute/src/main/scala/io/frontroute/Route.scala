package io.frontroute

import com.raquo.laminar.api.L._
import com.raquo.laminar.modifiers.Binder
import com.raquo.laminar.nodes.ReactiveElement
import io.frontroute.internal.LocationState
import io.frontroute.internal.RoutingState
import io.frontroute.internal.RouterStateRef
import io.frontroute.internal.SignalToStream

trait Route extends ((Location, RoutingState, RoutingState) => Signal[RouteResult]) with Mod[HtmlElement] {

  val id = Route.counter
  Route.counter = Route.counter + 1

  import Route._

  private val currentRender = Var(Option.empty[HtmlElement])

  private def bind: Binder[HtmlElement] = {
    Binder { el =>
      ReactiveElement.bindCallback(el) { ctx =>
        val locationState = LocationState.closestOrDefault(el.ref)
        val childStateRef = new RouterStateRef

        // the returned subscription will be managed by the ctx.owner
        val _ = SignalToStream(locationState.location)
          .collect { case Some(currentUnmatched) => currentUnmatched }
          .flatMap { currentUnmatched =>
            locationState.previousSiblingInProgress match {
              case Some(previousSiblingInProgress) =>
                previousSiblingInProgress.take(1).mapToStrict(currentUnmatched)
              case None                            =>
                EventStream.fromValue(currentUnmatched)
            }
          }
          .flatMap { currentUnmatched =>
            println(s"--------------- route: ${this.id}: $currentUnmatched -- ${locationState.isSiblingMatched()}")
            locationState.childStarted()

            SignalToStream(
              this.apply(
                currentUnmatched.copy(otherMatched = locationState.isSiblingMatched()),
                locationState.routerState.get(this).fold(RoutingState.empty)(_.resetPath),
                RoutingState.empty.withConsumed(locationState.consumed.now())
              )
            )
          }
          .flatMap {
            case RouteResult.Matched(nextState, location, consumed, createResult) =>
              println(s"RouteResult.Matched: ${this.id}: ${locationState.isSiblingMatched()}")
              locationState.resetChildMatched()
              println("locationState.notifySiblingMatched()")
              locationState.notifySiblingMatched()
              locationState.childFinished()
              if (
                !locationState.routerState.get(this).contains(nextState) ||
                currentRender.now().isEmpty
              ) {
                SignalToStream(createResult()).map(RouteEvent.NextRender(nextState, location, consumed, _))
              } else {
                EventStream.fromValue(RouteEvent.SameRender(nextState, location, consumed))
              }
            case RouteResult.RunEffect(nextState, location, consumed, run)        =>
              println(s"RouteResult.RunEffect: ${locationState.isSiblingMatched()}")
              locationState.notifySiblingMatched()
              locationState.childFinished()
              if (!locationState.routerState.get(this).contains(nextState)) {
                run()
                EventStream.fromValue(RouteEvent.SameRender(nextState, location, consumed))
              } else {
                EventStream.fromValue(RouteEvent.SameRender(nextState, location, consumed))
              }

            case RouteResult.Rejected =>
              println(s"RouteResult.Rejected: ${locationState.isSiblingMatched()}")
              locationState.childFinished()
              EventStream.fromValue(RouteEvent.NoRender)

          }
          .foreach {
            case RouteEvent.NextRender(nextState, remaining, consumed, render) =>
              locationState.routerState.set(this, nextState)

              locationState.setRemaining(Some(remaining))
              if (render != null) {
                val childState = LocationState.initIfMissing(
                  render.ref,
                  () =>
                    new LocationState(
                      location = locationState.remaining,
                      isSiblingMatched = locationState.isChildMatched,
                      resetSiblingMatched = locationState.resetChildMatched,
                      notifySiblingMatched = locationState.notifyChildMatched,
                      routerState = childStateRef,
                    )
                )
                childState.setConsumed(consumed)

                val amendedRender = render.amend(
                  onMountUnmountCallback(
                    ctx => childState.start()(ctx.owner),
                    _ => childState.kill()
                  ),
                )
                amendedRender.ref.dataset.addOne("frPath" -> consumed.mkString("/", "/", ""))
                currentRender.set(Some(amendedRender))
              } else {
                currentRender.set(None) // route matched but rendered a null
              }
            case RouteEvent.SameRender(nextState, remaining, consumed)         =>
              locationState.routerState.set(this, nextState)
              currentRender.now().foreach { render =>
                LocationState.closestOrDefault(render.ref).setConsumed(consumed)
                render.ref.dataset.addOne("frPath" -> consumed.mkString("/", "/", ""))
              }

              locationState.setRemaining(Some(remaining))
            case RouteEvent.NoRender                                           =>
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

  var counter = 1

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
