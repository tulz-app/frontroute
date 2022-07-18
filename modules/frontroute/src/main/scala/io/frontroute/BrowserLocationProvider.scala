package io.frontroute

import com.raquo.airstream.core.EventStream
import org.scalajs.dom

class BrowserLocationProvider(
  popStateEvents: EventStream[dom.PopStateEvent]
) extends LocationProvider {

  val locations: EventStream[RouteLocation] =
    popStateEvents.map { event => RouteLocation(dom.window.location, event.state) }

}
