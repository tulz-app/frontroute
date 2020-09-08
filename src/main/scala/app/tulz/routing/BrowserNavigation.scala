package app.tulz.routing

import org.scalajs.dom

import scala.scalajs.js

object BrowserNavigation {

  def pushState(statedata: js.Any, title: String, url: String): Unit = {
    dom.window.history.pushState(statedata, title, url)
    emitPopStateEvent()
  }

  def emitPopStateEvent(statedata: js.Any = js.undefined): Unit = {
    val event = js.Dynamic.newInstance(js.Dynamic.global.Event)("popstate", statedata).asInstanceOf[dom.PopStateEvent]
    val _     = dom.window.dispatchEvent(event)
  }

}
