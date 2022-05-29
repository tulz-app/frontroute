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
    popStateEvents: EventStream[dom.PopStateEvent]
  ): LocationProvider =
    new BrowserLocationProvider(popStateEvents = popStateEvents)

  @inline def custom(locations: Signal[String]) = new CustomLocationProvider(locations)

}
