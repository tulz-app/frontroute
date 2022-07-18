package io.frontroute

import com.raquo.laminar.api.L._

private[frontroute] object GlobalState {

  private var subscription: Subscription = null

  private val locationChangesBus                       = new EventBus[Unit]
  private val currentLocationVar                       = Var(Option.empty[RouteLocation])
  private var _currentLocation: Option[RouteLocation]  = None
  private var _currentUnmatched: Option[RouteLocation] = None

  val locationChanges: EventStream[Unit]      = locationChangesBus.events
  def currentUnmatched: Option[RouteLocation] = _currentUnmatched

  def setCurrentUnmatched(location: RouteLocation): Unit = {
    println(s"got new unmatched: $location")
    _currentUnmatched = Some(location)
  }

  def setLocationProvider(locationProvider: LocationProvider): Unit = {
    if (subscription != null) {
      subscription.kill()
      _currentLocation = None
      _currentUnmatched = None
    }
    subscription = locationProvider.locations.foreach { location =>
      println(s"got new location: $location")
      if (!_currentLocation.contains(location)) {
        val some = Some(location)
        _currentLocation = some
        setCurrentUnmatched(location)
        currentLocationVar.set(some)
        locationChangesBus.emit(())
      }
    }(unsafeWindowOwner)
  }

  def kill(): Unit = {
    if (subscription != null) {
      println("!! KILL")
      subscription.kill()
      _currentLocation = None
      _currentUnmatched = None
    }
  }

  def isActive(href: String): Signal[Boolean] = currentLocationVar.signal.map {
    case None    => false
    case Some(l) =>
      l.fullPath.mkString("/", "/", "").startsWith(href)

  }

}
