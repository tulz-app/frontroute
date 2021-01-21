package io.frontroute

import com.raquo.airstream.core.EventStream
import org.scalajs.dom

class BrowserLocationProvider(popStateEvents: EventStream[dom.PopStateEvent]) extends LocationProvider {

  val stream: EventStream[RouteLocation] = popStateEvents.map(RouteLocation(dom.window.location, _))

}
