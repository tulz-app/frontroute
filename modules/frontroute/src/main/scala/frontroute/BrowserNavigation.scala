package frontroute

import frontroute.domext.WindowWithScrollXY
import frontroute.internal.FrontrouteHistoryState
import frontroute.internal.HistoryState
import frontroute.internal.HistoryStateScrollPosition
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
    saveCurrentScrollPosition: Boolean
  ): HistoryState = {
    val internal = new FrontrouteHistoryState(
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
    url: js.UndefOr[String] = js.undefined,
    popStateEvent: Boolean = true
  ): Unit = {
    if (preserveScroll) {
      val newState = HistoryState.tryParse(dom.window.history.state) match {
        case Some(currentState) =>
          createHistoryState(
            user = currentState.user,
            saveCurrentScrollPosition = true
          )
        case None               =>
          createHistoryState(
            user = js.undefined,
            saveCurrentScrollPosition = true
          )
      }
      dom.window.history.replaceState(
        statedata = newState,
        title = ""
      )
    }
    val state = createHistoryState(
      user = data,
      saveCurrentScrollPosition = false
    )
    url.toOption match {
      case Some(url) =>
        dom.window.history.pushState(
          statedata = state,
          title = "",
          url = url
        )
      case None      =>
        dom.window.history.pushState(
          statedata = state,
          title = ""
        )
    }
    if (popStateEvent) {
      emitPopStateEvent(state)
    }
  }

  def replaceState(
    url: js.UndefOr[String] = js.undefined,
    data: js.Any = js.undefined,
    popStateEvent: Boolean = true
  ): Unit = {
    val state = createHistoryState(
      user = data,
      saveCurrentScrollPosition = false
    )
    url.toOption match {
      case Some(url) =>
        dom.window.history.replaceState(
          statedata = state,
          title = "",
          url = url
        )
      case None      =>
        dom.window.history.replaceState(
          statedata = state,
          title = ""
        )
    }
    if (popStateEvent) {
      emitPopStateEvent(state)
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
