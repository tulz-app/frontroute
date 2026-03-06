package frontroute

import frontroute.internal.HistoryState
import org.scalajs.dom
import scala.scalajs.js

final case class Location(
  hostname: String,
  port: String,
  protocol: String,
  host: String,
  origin: String,
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

  def apply(location: dom.Location, state: js.UndefOr[js.Any], baseName: String): Option[Location] = {
    extractPath(location, baseName).map { path =>
      var origin = s"${location.protocol}//${location.hostname}"
      if (location.port != "") {
        origin = s"${origin}:${location.port}"
      }
      new Location(
        hostname = location.hostname,
        port = location.port,
        protocol = location.protocol,
        host = location.host,
        origin = origin, // location.origin, TODO: location.origin was undefined in tests
        path = path,
        fullPath = path,
        params = LocationUtils.parseLocationParams(location),
        state = state,
        otherMatched = false
      )
    }
  }

  private def extractPath(location: dom.Location, baseName: String): Option[List[String]] = {
    val pathname = location.pathname
    if (pathname.startsWith(baseName + "/")) {
      Some(pathname.drop(baseName.length).dropWhile(_ == '/').split('/').toList.dropWhile(_.isEmpty))
    } else {
      None
    }
  }

}
