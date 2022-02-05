package io.frontroute

import io.frontroute.internal.HistoryState
import org.scalajs.dom
import scala.scalajs.js

final case class RouteLocation(
  hostname: String,
  port: String,
  protocol: String,
  host: String,
  origin: Option[String],
  unmatchedPath: List[String],
  params: Map[String, Seq[String]],
  state: js.UndefOr[js.Any]
) {

  @inline def withUnmatchedPath(path: List[String]): RouteLocation = this.copy(unmatchedPath = path)

  private[frontroute] val parsedState = HistoryState.tryParse(state)

  override def toString: String =
    s"${unmatchedPath.mkString("/")}${if (params.nonEmpty) "?" else ""}${params
        .flatMap { case (name, values) =>
          values.map(value => s"$name=$value")
        }
        .mkString("&")}"

}

object RouteLocation {

  def apply(location: dom.Location, state: js.UndefOr[js.Any]): RouteLocation =
    new RouteLocation(
      hostname = location.hostname,
      port = location.port,
      protocol = location.protocol,
      host = location.host,
      origin = location.origin.toOption,
      unmatchedPath = extractPath(location),
      params = LocationUtils.parseLocationParams(location),
      state = state
    )

  private def extractPath(location: dom.Location): List[String] = {
    location.pathname.dropWhile(_ == '/').split('/').toList.dropWhile(_.isEmpty)
  }

}
