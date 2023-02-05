package io.frontroute

import io.frontroute.internal.HistoryState
import org.scalajs.dom
import scala.scalajs.js

final case class Location(
  hostname: String,
  port: String,
  protocol: String,
  host: String,
  origin: Option[String],
  path: List[String],
  fullPath: List[String],
  params: Map[String, Seq[String]],
  state: js.UndefOr[js.Any],
  otherMatched: Boolean
) {

  @inline def withUnmatchedPath(path: List[String]): Location = this.copy(path = path)

  private[frontroute] val parsedState = HistoryState.tryParse(state)

  override def toString: String =
    s"path: '${path.mkString("/")}${if (params.nonEmpty) "?" else ""}${params
        .flatMap { case (name, values) =>
          values.map(value => s"$name=$value")
        }
        .mkString("&")}'"

}

object Location {

  def apply(location: dom.Location, state: js.UndefOr[js.Any]): Location = {
    val path = extractPath(location)
    new Location(
      hostname = location.hostname,
      port = location.port,
      protocol = location.protocol,
      host = location.host,
      origin = location.origin.toOption,
      path = path,
      fullPath = path,
      params = LocationUtils.parseLocationParams(location),
      state = state,
      otherMatched = false
    )
  }

  private def extractPath(location: dom.Location): List[String] = {
    location.pathname.dropWhile(_ == '/').split('/').toList.dropWhile(_.isEmpty)
  }

}
