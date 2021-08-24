package io.frontroute

import com.raquo.airstream.core.EventStream
import io.frontroute.internal.UrlString
import scala.scalajs.js

class CustomLocationProvider(locations: EventStream[String]) extends LocationProvider {

  val stream: EventStream[RouteLocation] = locations.collect { case UrlString(location) =>
    RouteLocation(location, js.undefined)
  }

}
