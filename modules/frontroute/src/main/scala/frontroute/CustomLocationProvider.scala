package frontroute

import com.raquo.laminar.api.L._
import frontroute.internal.UrlString

import scala.scalajs.js

class CustomLocationProvider(locationStrings: Signal[String]) extends LocationProvider {

  val current: Signal[Option[Location]] = locationStrings.map { case UrlString(location) =>
    Some(Location(location, js.undefined))
  }

  def start()(implicit owner: Owner): Subscription = new Subscription(owner, () => {})

}
