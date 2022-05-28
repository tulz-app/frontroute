package io.frontroute

import com.raquo.airstream.core.Signal
import io.frontroute.internal.UrlString

import scala.scalajs.js

class CustomLocationProvider(locations: Signal[String]) extends LocationProvider {

  val currentLocation: Signal[Option[RouteLocation]] = locations.map {
    case UrlString(location) => Some(RouteLocation(location, js.undefined))
    case _                   => None
  }

}
