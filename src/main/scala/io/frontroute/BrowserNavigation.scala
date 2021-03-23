package io.frontroute

import io.frontroute.domext.HistoryWithTitle
import io.frontroute.domext.WindowWithScrollXY
import io.frontroute.internal.FrontrouteHistoryState
import io.frontroute.internal.HistoryState
import io.frontroute.internal.HistoryStateScrollPosition
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.Dynamic

object BrowserNavigation {

  private val windowWithScrollXY = dom.window.asInstanceOf[WindowWithScrollXY]

  private var preserveScroll = true

  def preserveScroll(keep: Boolean): Unit =
    this.preserveScroll = keep

  private def getScrollPosition = {
    new HistoryStateScrollPosition(
      scrollX = windowWithScrollXY.scrollX,
      scrollY = windowWithScrollXY.scrollY
    )
  }

  private def createHistoryState(data: js.UndefOr[js.Any]): HistoryState = {
    val internal = new FrontrouteHistoryState()
    if (preserveScroll) {
      internal.scroll = getScrollPosition
    }
    new HistoryState(internal, data)
  }

  def pushState(
    data: js.Any = js.undefined,
    title: String = "",
    url: js.UndefOr[String] = js.undefined,
    popStateEvent: Boolean = true
  ): Unit = {
    if (preserveScroll) {
      HistoryState
        .tryParse(dom.window.history.state)
        .foreach { currentState =>
          val newInternal = currentState.internal.getOrElse(new FrontrouteHistoryState())
          val newState = new HistoryState(
            internal = newInternal,
            user = currentState.user
          )
          newInternal.scroll = getScrollPosition
          dom.window.history.replaceState(
            statedata = newState,
            title = dom.window.history.asInstanceOf[HistoryWithTitle].title.getOrElse("")
          )
        }
    }
    val state = createHistoryState(data)
    url.toOption match {
      case Some(url) =>
        dom.window.history.pushState(
          statedata = state,
          title = title,
          url = url
        )
      case None =>
        dom.window.history.pushState(
          statedata = state,
          title = title
        )
    }
    if (popStateEvent) {
      emitPopStateEvent(state)
    }
  }

  def replaceState(
    url: js.UndefOr[String] = js.undefined,
    title: String = "",
    data: js.Any = js.undefined,
    popStateEvent: Boolean = true
  ): Unit = {
    val state = createHistoryState(data)
    url.toOption match {
      case Some(url) =>
        dom.window.history.replaceState(
          statedata = state,
          title = title,
          url = url
        )
      case None =>
        dom.window.history.replaceState(
          statedata = state,
          title = title
        )
    }

    if (popStateEvent) {
      emitPopStateEvent(state)
    }
  }

  def emitPopStateEvent(state: js.Any = js.undefined): Unit = {
    val event = js.Dynamic.newInstance(js.Dynamic.global.Event)("popstate").asInstanceOf[Dynamic]
    event.state = state
    val _ = dom.window.dispatchEvent(event.asInstanceOf[dom.PopStateEvent])
  }

  def restoreScroll(): Unit = {
    if (preserveScroll) {
      HistoryState
        .tryParse(dom.window.history.state)
        .flatMap(_.internal.toOption)
        .flatMap(_.scroll.toOption)
        .foreach { scroll =>
          dom.window.scrollTo(scroll.scrollX.fold(0)(_.round.toInt), scroll.scrollY.fold(0)(_.round.toInt))
        }
    }
  }

}
