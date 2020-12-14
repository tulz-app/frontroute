package io.frontroute

import org.scalajs.dom

import scala.scalajs.js

object BrowserNavigation {

  def pushState(url: String, title: String, statedata: js.Any = js.undefined): Unit = {
    dom.window.history.pushState(statedata, title, url)
    emitPopStateEvent()
  }

  def replaceState(url: String, title: String, statedata: js.Any = js.undefined): Unit = {
    dom.window.history.replaceState(statedata, title, url)
    emitPopStateEvent()
  }

  def emitPopStateEvent(statedata: js.Any = js.undefined): Unit = {
    val event = js.Dynamic.newInstance(js.Dynamic.global.Event)("popstate", statedata).asInstanceOf[dom.PopStateEvent]
    val _     = dom.window.dispatchEvent(event)
  }

}
