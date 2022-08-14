package io.frontroute

import com.raquo.laminar.api.L._

private[frontroute] object DefaultLocationProvider {

  private val locationVar = Var(Option.empty[RouteLocation])

  val location: StrictSignal[Option[RouteLocation]] = locationVar.signal

  private var subscription: Subscription = LocationProvider.windowLocationProvider.changes.foreach { _ =>
    locationVar.set(LocationProvider.windowLocationProvider.current)
  }(unsafeWindowOwner)

  def set(locationProvider: LocationProvider): Unit = {
    if (subscription != null) {
      subscription.kill()
    }
    subscription = locationProvider.changes.foreach { _ =>
      locationVar.set(locationProvider.current)
    }(unsafeWindowOwner)
  }

  def isActive(href: String): Signal[Boolean] = location.map {
    case None    => false
    case Some(l) =>
      l.fullPath.mkString("/", "/", "").startsWith(href)
  }

}
