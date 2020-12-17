package io.frontroute

import com.raquo.airstream.eventstream.EventStream
import org.scalajs.dom
import org.scalajs.dom.raw.Location

import scala.scalajs.js

trait RouteLocationProvider {

  def stream: EventStream[RouteLocation]

}
