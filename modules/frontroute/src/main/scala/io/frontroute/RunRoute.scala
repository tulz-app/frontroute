package io.frontroute

import com.raquo.laminar.api.L._
import io.frontroute.internal.RoutingState

trait RunRoute {

  def runRoute(
    route: Route
  )(implicit owner: Owner): Signal[Option[Element]] = {
//    println(s"run route")
    var currentState                      = RoutingState.empty
    var currentSubscription: Subscription = null
    val currentResult                     = Var(Option.empty[Element])
    var previous                          = Option.empty[RouteLocation]
    val myDeepness                        = GlobalState.deepness

    EventStream
      .merge(
        EventStream.fromValue(()),
        GlobalState.locationChanges
      )
//      .map { _ =>
//        println(s"!!!! deepness:         ${GlobalState.deepness}")
//        println(s"!!   my deepness:      ${myDeepness}")
//        println(s"     currentUnmatched: ${GlobalState.currentUnmatched}")
//        println(s"     previous:         ${previous}")
//      }
      .collect {
        case _ if GlobalState.deepness == myDeepness && !previous.contains(GlobalState.currentLocation) =>
          previous = Some(GlobalState.currentLocation)
          ()
      }
      .flatMap { _ =>
        route(
          GlobalState.currentUnmatched,
          currentState.resetPath,
          RoutingState.withPersistentData(currentState.persistent, currentState.async)
        ).map {
          case RouteResult.Complete(nextState, location, createResult) =>
//            println(s"nextState: $nextState")
//            println(s"currentState: $currentState")
//            println(s"next location: $location")
            GlobalState.setCurrentUnmatched(location)
            if (nextState != currentState) {
//              println(s"!! state changed")
              currentState = nextState
              Some(createResult)
            } else {
              Option.empty
            }
          case RouteResult.Rejected                                    =>
            Option.empty
        }
      }
      .map { r =>
        GlobalState.setDeepness(myDeepness + 1)
        GlobalState.emit()
        r
      }
      .collect { case Some(createView) =>
//        println(s"matched: $myDeepness")
        createView()
      }
      .foreach { createResult =>
        if (currentSubscription != null) {
          currentSubscription.kill()
          currentSubscription = null
        }
        currentSubscription = createResult.foreach { result =>
          currentResult.set(Option(result))
        }
      }

    GlobalState.emit()
    currentResult.signal
  }

}
