package io.frontroute

import com.raquo.laminar.api.L._
import org.scalajs.dom

import scala.scalajs.js

class BrowserLocationProvider(
  popStateEvents: EventStream[dom.PopStateEvent]
) extends LocationProvider {

  private val currentVar                = Var(Option.empty[Location])
  val current: Signal[Option[Location]] = currentVar.signal

  def start()(implicit owner: Owner): Subscription = {
    EventStream
      .merge(
        popStateEvents.map(_.state),
        EventStream.fromValue(js.undefined: js.Any)
      )
      .map(state => Location(dom.window.location, state))
      .foreach { l =>
        currentVar.set(Some(l))
      }
  }

}
