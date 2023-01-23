package io.frontroute.site

import org.scalajs.dom

class Wiring(
  val ssrContext: SsrContext,
  val routes: Routes
)

object Wiring {

  def apply(): Wiring = {
    new Wiring(
      ssrContext = SsrContext(
        ssr = dom.window.navigator.userAgent == "frontroute/ssr"
      ),
      routes = new Routes
    )
  }

}
