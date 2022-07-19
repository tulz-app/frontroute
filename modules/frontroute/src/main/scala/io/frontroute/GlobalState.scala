package io.frontroute

import com.raquo.laminar.api.L._

private[frontroute] object GlobalState {

  private var subscription: Subscription = null

  private var _deepness = 0
  def deepness: Int     = _deepness

  private val locationChangesBus               = new EventBus[Unit]
  private val currentLocationVar               = Var(Option.empty[RouteLocation])
  private var _currentLocation: RouteLocation  = RouteLocation.emoty
  private var _currentUnmatched: RouteLocation = RouteLocation.emoty

  val locationChanges: EventStream[Unit] = locationChangesBus.events
  def currentUnmatched: RouteLocation    = _currentUnmatched
  def currentLocation: RouteLocation     = _currentLocation

  def setCurrentUnmatched(location: RouteLocation): Unit = {
//    println(s"got new unmatched: $location")
    _currentUnmatched = location
  }

  def emit(): Unit = {
//    println("emitting")
    locationChangesBus.emit(())
  }

  def setLocationProvider(locationProvider: LocationProvider): Unit = {
    if (subscription != null) {
      subscription.kill()
      _currentLocation = locationProvider.current
      _currentUnmatched = locationProvider.current
    }
    subscription = locationProvider.changes.foreach { _ =>
      val location = locationProvider.current
//      println(s"got new location: ${locationProvider.current}")
      setDeepness(0)
      if (_currentLocation != location) {
        _currentLocation = location
        setCurrentUnmatched(location)
        currentLocationVar.set(Some(location))
        emit()
      }
    }(unsafeWindowOwner)
  }

  def setDeepness(d: Int): Unit = {
//    println(s"deepness <-- $d")
    _deepness = d
  }

  def kill(): Unit = {
    if (subscription != null) {
//      println("!! KILL")
      subscription.kill()
      subscription = null
      _currentLocation = RouteLocation.emoty
      _currentUnmatched = RouteLocation.emoty
      _deepness = 0
    }
  }

  def isActive(href: String): Signal[Boolean] = currentLocationVar.signal.map {
    case None    => false
    case Some(l) =>
      l.fullPath.mkString("/", "/", "").startsWith(href)

  }

}
