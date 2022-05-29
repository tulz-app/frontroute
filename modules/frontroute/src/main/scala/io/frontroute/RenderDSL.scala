package io.frontroute

import com.raquo.laminar.api.L._

trait RenderDSL extends RouteDSL[Element] {

  implicit def elementToRoute(e: Element): Route                = complete(e)
  implicit def signlOfElementToRoute(e: Signal[Element]): Route = complete(e)

}
