package io.frontroute

import com.raquo.laminar.api.L._
import com.raquo.airstream.core.EventStream
import org.scalajs.dom

trait LocationProvider {

  def currentLocation: Signal[Option[RouteLocation]]

}

object LocationProvider {

  lazy val windowLocationProvider: LocationProvider = LocationProvider.browser(windowEvents.onPopState)

  @inline def browser(
    popStateEvents: EventStream[dom.PopStateEvent],
    setTitleOnPopStateEvents: Boolean = true,
    updateTitleElement: Boolean = true,
    ignoreEmptyTitle: Boolean = false
  ): LocationProvider = new BrowserLocationProvider(
    popStateEvents = popStateEvents,
    setTitleOnPopStateEvents = setTitleOnPopStateEvents,
    updateTitleElement = updateTitleElement,
    ignoreEmptyTitle = ignoreEmptyTitle
  )

  @inline def custom(locations: Signal[String]) = new CustomLocationProvider(locations)

}
