package io.frontroute

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Signal
import io.frontroute.internal.DocumentTitle
import org.scalajs.dom

class BrowserLocationProvider(
  popStateEvents: EventStream[dom.PopStateEvent],
  setTitleOnPopStateEvents: Boolean = true,
  updateTitleElement: Boolean = true,
  ignoreEmptyTitle: Boolean = false
) extends LocationProvider {

  val currentLocation: Signal[Option[RouteLocation]] = popStateEvents.map { event =>
    val routeLocation = RouteLocation(dom.window.location, event.state)
    if (setTitleOnPopStateEvents) {
      routeLocation.parsedState.foreach { state =>
        state.internal.foreach { internal =>
          DocumentTitle.updateTitle(title = internal.title, updateTitleElement = updateTitleElement, ignoreEmptyTitle = ignoreEmptyTitle)
        }
      }
    }
    routeLocation
  }.toWeakSignal

}
