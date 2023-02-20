package frontroute.testing

import com.raquo.laminar.api.L._
import frontroute.LocationProvider
import frontroute.Location
import frontroute.internal.HistoryState

import scala.scalajs.js

class TestLocationProvider extends LocationProvider {

  private var currentProtocol                          = "https"
  private var currentHostname                          = "test.nowhere"
  private var currentPort                              = "443"
  private var currentPath: List[String]                = List.empty
  private var currentParams: Map[String, List[String]] = Map.empty
  private var currentState: js.UndefOr[HistoryState]   = js.undefined

  private val _current                  = Var(Option.empty[Location])
  def current: Signal[Option[Location]] = _current.signal

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
    _current.set(
      Some(
        Location(
          hostname = currentHostname,
          port = currentPort,
          protocol = currentProtocol,
          host = s"${currentHostname}:${currentPort}",
          origin = Some(s"${currentProtocol}://${currentHostname}:${currentPort}"),
          path = currentPath,
          fullPath = currentPath,
          params = currentParams,
          state = currentState,
          otherMatched = false,
        )
      )
    )
  }

}
