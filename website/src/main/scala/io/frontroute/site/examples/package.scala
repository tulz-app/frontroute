package io.frontroute.site

import com.raquo.laminar.api.L._
import io.frontroute.LocationProvider

package object examples {

  type TitledExample[E <: Element] = (String, sourcecode.Text[E])

  def useLocationProvider(p: LocationProvider)(body: LocationProvider => Element): Element = body(p)

}
