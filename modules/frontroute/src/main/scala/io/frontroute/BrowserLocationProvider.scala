package io.frontroute

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Signal
import org.scalajs.dom

class BrowserLocationProvider(
  popStateEvents: EventStream[dom.PopStateEvent]
) extends LocationProvider {

  val currentLocation: Signal[Option[RouteLocation]] =
    popStateEvents.map { event => RouteLocation(dom.window.location, event.state) }.toWeakSignal

}
