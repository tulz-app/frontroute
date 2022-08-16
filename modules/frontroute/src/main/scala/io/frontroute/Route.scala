package io.frontroute

import com.raquo.laminar.api.L._
import com.raquo.laminar.lifecycle.InsertContext
import io.frontroute.internal.ElementWithLocationState
import io.frontroute.internal.LocationState
import io.frontroute.internal.RoutingState
import io.frontroute.internal.RoutingStateRef
import io.frontroute.internal.SignalToStream

trait Route extends ((RouteLocation, RoutingState, RoutingState) => Signal[RouteResult]) with Mod[HtmlElement] {

  import Route._

  override def apply(element: HtmlElement): Unit = {
    var maybeSubscription: Option[DynamicSubscription] = None
    val lockedContext                                  = InsertContext.reserveSpotContext[HtmlElement](element)

    element.amend(
      onMountUnmountCallback[HtmlElement](
        mount = { c =>
          import c.owner
          val inserter = (child.maybe <-- runRoute(this, c)).withContext(lockedContext)
          maybeSubscription = Some(inserter.bind(c.thisNode))
        },
        unmount = { _ =>
          maybeSubscription.foreach(_.kill())
          maybeSubscription = None
        }
      )
    )
  }

  private def runRoute(
    route: Route,
    ctx: MountContext[HtmlElement]
  )(implicit owner: Owner): Signal[Option[Element]] = {
    val currentRender = Var(Option.empty[Element])
    val locationState = ElementWithLocationState.getClosestOrInit(
      ctx.thisNode.ref,
      () => {
        val siblingMatched   = Var(false)
        val onSiblingMatched = siblingMatched.writer.contramap { (_: Unit) => true }
        DefaultLocationProvider.location.foreach { _ => siblingMatched.set(false) }
        new LocationState(
          DefaultLocationProvider.location,
          siblingMatched.signal,
          onSiblingMatched,
          new RoutingStateRef,
          owner
        ).start()
      }
    )
    val childStateRef = new RoutingStateRef

    def killPrevious(): Unit = {
      currentRender.now().foreach { previous =>
        previous.ref.asInstanceOf[ElementWithLocationState].____locationState.foreach(_.kill())
      }
    }

    val _ =
      SignalToStream(locationState.location)
        .delay(0)
        .collect { case Some(currentUnmatched) =>
          currentUnmatched
        }
        .flatMap { currentUnmatched =>
          val routingState = locationState.currentState.get(this).getOrElse(RoutingState.empty)
          SignalToStream(
            route(
              currentUnmatched.copy(otherMatched = locationState.siblingMatched),
              routingState.resetPath,
              RoutingState.empty
//              RoutingState.withPersistentData()
            )
          )
        }
        .flatMap {
          case RouteResult.Matched(nextState, location, createResult) =>
            val routingState = locationState.currentState.get(this)
            if (!routingState.contains(nextState) || currentRender.now().isEmpty) {
              SignalToStream(
                createResult().map { result =>
                  RouteEvent.NextRender(nextState, location, result)
                }
              )
            } else {
              EventStream.fromValue(
                RouteEvent.SameRender(nextState, location)
              )
            }

          case RouteResult.Rejected =>
            EventStream.fromValue(
              RouteEvent.NoRender
            )
        }
        .foreach {
          case RouteEvent.NextRender(nextState, remaining, render) =>
            killPrevious()
            val _ = ElementWithLocationState.getOrInit(
              render.ref,
              () =>
                new LocationState(
                  locationState.remaining,
                  locationState.$childMatched,
                  locationState.onChildMatched,
                  childStateRef,
                  owner
                ).start()
            )

            locationState.currentState.set(this, nextState)

            locationState.emitRemaining(Some(remaining))
            locationState.notifyMatched()

            currentRender.set(Some(render))

          case RouteEvent.SameRender(nextState, remaining) =>
            locationState.currentState.set(this, nextState)

            locationState.emitRemaining(Some(remaining))
            locationState.notifyMatched()

          case RouteEvent.NoRender =>
            killPrevious()
            locationState.currentState.unset(this)
            currentRender.set(None)
        }

    currentRender.signal
  }

}

object Route {

  implicit def toDirective[L](route: Route): Directive[L] = Directive[L](_ => route)

  sealed private[frontroute] trait RouteEvent extends Product with Serializable

  private[frontroute] object RouteEvent {

    case class NextRender(
      nextState: RoutingState,
      remaining: RouteLocation,
      render: Element
    ) extends RouteEvent

    case class SameRender(
      nextState: RoutingState,
      remaining: RouteLocation
    ) extends RouteEvent

    case object NoRender extends RouteEvent

  }

}
