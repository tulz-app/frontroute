package io.frontroute

import com.raquo.airstream.core.EventStream
import io.frontroute.internal.UrlString

import scala.scalajs.js

class CustomLocationProvider(locationStrings: EventStream[String]) extends LocationProvider {

  private var _current = Option.empty[Location]

  def current: Option[Location] = _current

  val changes: EventStream[Unit] = locationStrings.collect { case UrlString(location) =>
    _current = Some(Location(location, js.undefined))
  }

}
