package io.frontroute

import com.raquo.airstream.core.EventStream
import io.frontroute.internal.UrlString

import scala.scalajs.js

class CustomLocationProvider(locationStrings: EventStream[String]) extends LocationProvider {

  val locations: EventStream[RouteLocation] = locationStrings.collect { case UrlString(location) =>
    RouteLocation(location, js.undefined)
  }

}
