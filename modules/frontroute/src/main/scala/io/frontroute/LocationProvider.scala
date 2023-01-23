package io.frontroute

import com.raquo.laminar.api.L._
import com.raquo.airstream.core.EventStream
import org.scalajs.dom

trait LocationProvider {

  def current: Signal[Option[Location]]
  def start()(implicit owner: Owner): Subscription

}

object LocationProvider {

  lazy val windowLocationProvider: LocationProvider = browser(windowEvents(_.onPopState))

  @inline def browser(popStateEvents: EventStream[dom.PopStateEvent]): LocationProvider = new BrowserLocationProvider(popStateEvents)

  @inline def custom(locationStrings: Signal[String]) = new CustomLocationProvider(locationStrings)

}
