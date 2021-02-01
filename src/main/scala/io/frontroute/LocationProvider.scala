package io.frontroute

import com.raquo.airstream.core.EventStream
import org.scalajs.dom

trait LocationProvider {

  def stream: EventStream[RouteLocation]

}

object LocationProvider {

  @inline def browser(popStateEvents: EventStream[dom.PopStateEvent]): LocationProvider = new BrowserLocationProvider(popStateEvents)
  @inline def custom(locations: EventStream[String])                                    = new CustomLocationProvider(locations)

}
