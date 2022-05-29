package io.frontroute

import io.frontroute.domext.WindowWithScrollXY
import io.frontroute.internal.DocumentMeta
import io.frontroute.internal.FrontrouteHistoryState
import io.frontroute.internal.HistoryState
import io.frontroute.internal.HistoryStateScrollPosition
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.Dynamic

object BrowserNavigation {

  private val windowWithScrollXY = dom.window.asInstanceOf[WindowWithScrollXY]

  private var preserveScroll = true

  def preserveScroll(keep: Boolean): Unit = {
    this.preserveScroll = keep
  }

  private def currentScrollPosition(): HistoryStateScrollPosition = {
    new HistoryStateScrollPosition(
      scrollX = windowWithScrollXY.scrollX,
      scrollY = windowWithScrollXY.scrollY
    )
  }

  private def createHistoryState(
    user: js.UndefOr[js.Any],
    meta: PageMeta,
    saveCurrentScrollPosition: Boolean
  ): HistoryState = {
    val internal = new FrontrouteHistoryState(
      meta = meta,
      scroll = if (saveCurrentScrollPosition) {
        currentScrollPosition()
      } else {
        js.undefined
      }
    )

    new HistoryState(internal = internal, user = user)
  }

  def pushState(
    data: js.Any = js.undefined,
    meta: PageMeta = PageMeta.empty,
    url: js.UndefOr[String] = js.undefined,
    popStateEvent: Boolean = true
  ): Unit = {
    if (preserveScroll) {
      val newState = HistoryState.tryParse(dom.window.history.state) match {
        case Some(currentState) =>
          createHistoryState(
            user = currentState.user,
            meta = currentState.internal.fold(PageMeta.empty)(_.meta),
            saveCurrentScrollPosition = true
          )
        case None               =>
          createHistoryState(
            user = js.undefined,
            meta = PageMeta.empty,
            saveCurrentScrollPosition = true
          )
      }
      dom.window.history.replaceState(
        statedata = newState,
        title = newState.internal.fold("")(_.meta.title)
      )
    }
    val state = createHistoryState(
      user = data,
      meta = meta,
      saveCurrentScrollPosition = false
    )
    url.toOption match {
      case Some(url) =>
        dom.window.history.pushState(
          statedata = state,
          title = meta.title,
          url = url
        )
      case None      =>
        dom.window.history.pushState(
          statedata = state,
          title = meta.title
        )
    }
    if (popStateEvent) {
      emitPopStateEvent(state)
    }
  }

  def replaceState(
    url: js.UndefOr[String] = js.undefined,
    meta: PageMeta = PageMeta.empty,
    data: js.Any = js.undefined,
    popStateEvent: Boolean = true
  ): Unit = {
    val state = createHistoryState(
      user = data,
      meta = meta,
      saveCurrentScrollPosition = false
    )
    url.toOption match {
      case Some(url) =>
        dom.window.history.replaceState(
          statedata = state,
          title = meta.title,
          url = url
        )
      case None      =>
        dom.window.history.replaceState(
          statedata = state,
          title = meta.title
        )
    }
    if (popStateEvent) {
      emitPopStateEvent(state)
    }
  }

  def replacePageMeta(
    meta: PageMeta,
    popStateEvent: Boolean = false
  ): Unit = {
    val newState = HistoryState.tryParse(dom.window.history.state) match {
      case Some(currentState) =>
        val newInternal = new FrontrouteHistoryState(meta = meta, scroll = currentState.internal.flatMap(_.scroll))
        new HistoryState(
          internal = newInternal,
          user = currentState.user
        )
      case None               =>
        new HistoryState(
          internal = new FrontrouteHistoryState(meta = meta, scroll = js.undefined),
          user = js.undefined
        )
    }
    dom.window.history.replaceState(
      statedata = newState,
      title = meta.title
    )
    DocumentMeta.update(meta)
    if (popStateEvent) {
      emitPopStateEvent(newState)
    }
  }

  def emitPopStateEvent(state: js.Any = js.undefined): Unit = {
    val event = js.Dynamic.newInstance(js.Dynamic.global.Event)("popstate").asInstanceOf[Dynamic]
    event.state = state
    val _     = dom.window.dispatchEvent(event.asInstanceOf[dom.PopStateEvent])
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
