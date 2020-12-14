package io.frontroute

import com.raquo.airstream.eventstream.EventStream
import org.scalajs.dom
import org.scalajs.dom.raw.Location

import scala.scalajs.js

trait RouteLocationProvider {

  def stream: EventStream[RouteLocation]

}

class BrowserRouteLocationProvider(
  $popStateEvent: EventStream[dom.PopStateEvent]
) extends RouteLocationProvider {

  val stream: EventStream[RouteLocation] =
    $popStateEvent.mapTo(extractRouteLocation)

  private def extractRouteLocation = {
    RouteLocation(
      extractPath(dom.window.location),
      extractParams(dom.window.location)
    )
  }

  private def extractPath(location: Location): List[String] = {
    location.pathname.dropWhile(_ == '/').split('/').toList.dropWhile(_.isEmpty)
  }

  private def extractParams(location: Location): Map[String, Seq[String]] = {
    val vars   = location.search.dropWhile(_ == '?').split('&')
    val result = scala.collection.mutable.Map[String, Seq[String]]()
    vars.foreach { entry =>
      entry.split('=') match {
        case Array(key, value) =>
          val decodedKey   = js.URIUtils.decodeURIComponent(key)
          val decodedValue = js.URIUtils.decodeURIComponent(value)
          result(decodedKey) = result.getOrElse(decodedKey, Seq.empty) :+ decodedValue
        case _ =>
      }
    }
    result.toMap
  }

}
