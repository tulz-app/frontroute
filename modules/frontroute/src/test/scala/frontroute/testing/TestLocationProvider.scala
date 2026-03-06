package frontroute.testing

import com.raquo.laminar.api.L.*
import frontroute.LocationProvider
import frontroute.Location
import frontroute.internal.HistoryState
import frontroute.internal.UrlString

import scala.scalajs.js
import scala.scalajs.js.URIUtils.encodeURIComponent

class TestLocationProvider(val baseName: String = "") extends LocationProvider {

  private var currentProtocol                          = "https"
  private var currentHostname                          = "test.nowhere"
  private var currentPort                              = "443"
  private var currentPath: List[String]                = List.empty
  private var currentParams: Map[String, List[String]] = Map.empty
  private var currentState: js.UndefOr[HistoryState]   = js.undefined

  private val _current                                = Var(Option.empty[Either[Unit, Location]])
  def current: Signal[Option[Either[Unit, Location]]] = _current.signal

  def start()(implicit owner: Owner): Subscription = new Subscription(owner, () => {})

  def protocol(protocol: String): Unit = {
    currentProtocol = protocol
    emit()
  }

  def hostname(hostname: String): Unit = {
    currentHostname = hostname
    emit()
  }

  def port(port: String): Unit = {
    currentPort = port
    emit()
  }

  def path(parts: String*): Unit = {
    currentPath = parts.toList
    emit()
  }

  def params(params: (String, String)*): Unit = {
    currentParams = params
      .groupBy(_._1)
      .view
      .map { case (name, values) =>
        name -> values.map(_._2).toList
      }
      .toMap
    emit()
  }

  def state(userState: js.UndefOr[js.Any]): Unit = {
    currentState = new HistoryState(internal = js.undefined, user = userState)
    emit()
  }

  def emit(): Unit = {
    val query =
      currentParams.toSeq
        .flatMap { case (key, values) =>
          values.map { value =>
            s"${encodeURIComponent(key)}=${encodeURIComponent(value)}"
          }
        }.mkString("?", "&", "")

    val locationString      = s"${currentProtocol}://${currentHostname}:${currentPort}${currentPath.mkString("/", "/", "")}${query}"
//    println(s"LOCATION: ${locationString}")
    val UrlString(location) = locationString
//    org.scalajs.dom.console.log(s"PARSED LOCATION", location)
    Location(location, currentState, baseName) match {
      case Some(location) => _current.set(Some(Right(location)))
      case None           => _current.set(Some(Left(())))
    }
  }

}
