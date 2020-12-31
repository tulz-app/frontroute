package io.frontroute

import com.raquo.airstream.eventstream.EventStream

trait RouteLocationProvider {

  def stream: EventStream[RouteLocation]

}
