package io.frontroute

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Signal
import io.frontroute.internal.DocumentMeta
import org.scalajs.dom

class BrowserLocationProvider(
  popStateEvents: EventStream[dom.PopStateEvent],
  updatePageMetaOnPopStateEvents: Boolean = true
) extends LocationProvider {

  val currentLocation: Signal[Option[RouteLocation]] = popStateEvents.map { event =>
    val routeLocation = RouteLocation(dom.window.location, event.state)
    if (updatePageMetaOnPopStateEvents) {
      routeLocation.parsedState.foreach { state =>
        state.internal.foreach { internal =>
          DocumentMeta.update(internal.meta)
        }
      }
    }
    routeLocation
  }.toWeakSignal

}
