package io

import com.raquo.laminar.api.L._

package object frontroute {

  type PathMatcher0 = Types.PathMatcher0

  def dsl[A]: RouteDSL[A] = new RouteDSL[A] {}

  object renderDSL extends RouteDSL[Element]

  object Implicits {

    implicit val locationProvider: LocationProvider = LocationProvider.windowLocationProvider

  }

}
