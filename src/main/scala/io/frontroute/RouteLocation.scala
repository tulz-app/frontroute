package io.frontroute

import io.frontroute.internal.HistoryState
import org.scalajs.dom.raw

import scala.scalajs.js

final case class RouteLocation(
  hostname: String,
  port: String,
  protocol: String,
  host: String,
  origin: Option[String],
  unmatchedPath: List[String],
  params: Map[String, Seq[String]],
  state: Option[HistoryState]
) {

  @inline def withUnmatchedPath(path: List[String]): RouteLocation = this.copy(unmatchedPath = path)

  override def toString: String =
    s"${unmatchedPath.mkString("/")}${if (params.nonEmpty) "?" else ""}${params
      .flatMap { case (name, values) =>
        values.map(value => s"$name=$value")
      }
      .mkString("&")}"

}

object RouteLocation {

  def apply(location: raw.Location, state: js.Any): RouteLocation = {
    RouteLocation(
      hostname = location.hostname,
      port = location.port,
      protocol = location.protocol,
      host = location.host,
      origin = location.origin.toOption,
      unmatchedPath = extractPath(location),
      params = extractParams(location),
      state = HistoryState.tryParse(state)
    )

  }

  private def extractPath(location: raw.Location): List[String] = {
    location.pathname.dropWhile(_ == '/').split('/').toList.dropWhile(_.isEmpty)
  }

  private def extractParams(location: raw.Location): Map[String, Seq[String]] = {
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
