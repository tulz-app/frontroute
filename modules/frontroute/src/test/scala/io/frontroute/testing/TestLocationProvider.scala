package io.frontroute.testing

import com.raquo.airstream.core.Signal
import com.raquo.airstream.eventbus.EventBus
import io.frontroute.LocationProvider
import io.frontroute.RouteLocation
import io.frontroute.internal.HistoryState

import scala.scalajs.js

class TestLocationProvider extends LocationProvider {

  private var currentProtocol                          = "https"
  private var currentHostname                          = "test.nowhere"
  private var currentPort                              = "443"
  private var currentPath: List[String]                = List.empty
  private var currentParams: Map[String, List[String]] = Map.empty
  private var currentState: js.UndefOr[HistoryState]   = js.undefined

  private val bus = new EventBus[RouteLocation]

  val currentLocation: Signal[Option[RouteLocation]] = bus.events.toWeakSignal

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
    bus.writer.onNext(
      RouteLocation(
        hostname = currentHostname,
        port = currentPort,
        protocol = currentProtocol,
        host = s"${currentHostname}:${currentPort}",
        origin = Some(s"${currentProtocol}://${currentHostname}:${currentPort}"),
        unmatchedPath = currentPath,
        params = currentParams,
        state = currentState
      )
    )
  }

}
