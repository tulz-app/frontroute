package io.frontroute

import com.raquo.airstream.core.EventStream
import io.frontroute.internal.UrlString

import scala.scalajs.js

class CustomLocationProvider(initial: String, locationStrings: EventStream[String]) extends LocationProvider {

  private var _current = RouteLocation(
    initial match {
      case UrlString(location) => location
      case other               => throw new RuntimeException(s"invalid initial location: $other")
    },
    js.undefined
  )

  def current: RouteLocation = _current

  val changes: EventStream[Unit] = locationStrings.collect { case UrlString(location) =>
    _current = RouteLocation(location, js.undefined)
  }

}
