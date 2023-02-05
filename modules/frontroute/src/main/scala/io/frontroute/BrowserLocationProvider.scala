package io.frontroute

import com.raquo.laminar.api.L._
import org.scalajs.dom

import scala.scalajs.js

class BrowserLocationProvider(
  popStateEvents: EventStream[dom.PopStateEvent]
) extends LocationProvider {

  private val currentVar                = Var(Option.empty[Location])
  val current: Signal[Option[Location]] = currentVar.signal.debugWithName("xxxxxxxxxxx BrowserLocationProvider location").debugLogEvents()

  def start()(implicit owner: Owner): Subscription = {
    println("!!!!!!!!!!!!!!!!!!!!!!!! BrowserLocationProvider start")
    EventStream
      .merge(
        popStateEvents.map(_.state),
        EventStream.fromValue(js.undefined: js.Any)
      )
      .map(state => Location(dom.window.location, state))
      .foreach { l =>
        println(s"new location: $l")
        currentVar.set(Some(l))
      }
  }

//  val current: Signal[Option[Location]] =
//    popStateEvents
//      .map { e =>
//        Location(dom.window.location, e.state)
//      }
//      .toSignal(
//        Location(dom.window.location, js.undefined)
//      )
//      .map(Some(_))
//      .debugWithName("xxxxxxxxxxx BrowserLocationProvider location").debugLogEvents()
//
//  def start()(implicit owner: Owner): Subscription = {
//    null
//  }

}
