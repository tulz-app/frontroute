package io.frontroute

import com.raquo.airstream.core.Signal
import io.frontroute.internal.UrlString

import scala.scalajs.js

class CustomLocationProvider(locations: Signal[Option[String]]) extends LocationProvider {

  val currentLocation: Signal[Option[RouteLocation]] = locations.map {
    case Some(UrlString(location)) => Some(RouteLocation(location, js.undefined))
    case None                      => None
  }

}
