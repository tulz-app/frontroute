package frontroute

import com.raquo.laminar.api.L.*
import frontroute.internal.UrlString

import scala.scalajs.js

class CustomLocationProvider(locationStrings: Signal[String], val baseName: String) extends LocationProvider {

  val current: Signal[Option[Location]] = locationStrings.map { case UrlString(location) =>
    Location(location, js.undefined, baseName)
  }

  def start()(implicit owner: Owner): Subscription = new Subscription(owner, () => {})

}
