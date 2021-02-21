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
      params = LocationUtils.parseLocationParams(location),
      state = HistoryState.tryParse(state)
    )

  }

  private def extractPath(location: raw.Location): List[String] = {
    location.pathname.dropWhile(_ == '/').split('/').toList.dropWhile(_.isEmpty)
  }

}
