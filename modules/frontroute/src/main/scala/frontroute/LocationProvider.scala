package frontroute

import com.raquo.laminar.api.L.*
import com.raquo.airstream.core.EventStream
import org.scalajs.dom

trait LocationProvider {

  def baseName: String
  def current: Signal[Option[Location]]
  def start()(implicit owner: Owner): Subscription

}

object LocationProvider {

  lazy val windowLocationProvider: LocationProvider = windowLocationProvider(baseName = "")

  def windowLocationProvider(baseName: String): LocationProvider = browser(windowEvents(_.onPopState), baseName)

  def browser(popStateEvents: EventStream[dom.PopStateEvent], baseName: String): LocationProvider = new BrowserLocationProvider(popStateEvents.delay(0), baseName)

  def custom(locationStrings: Signal[String], baseName: String) = new CustomLocationProvider(locationStrings, baseName)

}
