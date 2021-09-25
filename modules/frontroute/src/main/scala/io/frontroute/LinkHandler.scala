package io.frontroute

import io.frontroute.internal.UrlString
import org.scalajs.dom
import org.scalajs.dom.raw._
import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

object LinkHandler {

  @js.native
  @JSGlobal("window")
  private object WindowWithRouteTo extends js.Object {
    var routeTo: js.UndefOr[js.Function1[String, Unit]] = js.native
  }

  private def clickListener(defaultTitle: String): js.Function1[Event, Unit] = event => {
    findParent("A", event.target.asInstanceOf[dom.Node]).foreach { aParent =>
      val anchor     = aParent.asInstanceOf[HTMLAnchorElement]
      val rel        = anchor.rel
      val href       = anchor.href
      val title      = anchor.dataset.get("title")
      val sameOrigin =
        href.startsWith("/") ||
          !href.startsWith("http://") && !href.startsWith("https://") ||
          dom.window.location.origin.exists(origin => href.startsWith(origin))

      if (sameOrigin && (js.isUndefined(rel) || rel == null || rel == "")) {
        event.preventDefault()
        val shouldPush = UrlString.unapply(anchor.href).fold(true) { location =>
          location.pathname != dom.window.location.pathname ||
          location.search != dom.window.location.search ||
          location.hash != dom.window.location.hash
        }
        if (shouldPush) {
          BrowserNavigation.pushState(url = anchor.href, title = title.getOrElse(defaultTitle))
        }
      } else if (rel == "external") {
        event.preventDefault()
        dom.window.open(anchor.href)
      }
    }
  }

  private val routeTo: js.Function1[String, Unit]                            = (path: String) => BrowserNavigation.pushState(url = path)

  def install(
    defaultTitle: String = ""
  ): js.Function1[Event, Unit] = {
    WindowWithRouteTo.routeTo = routeTo
    val listener = clickListener(defaultTitle)
    dom.document.addEventListener("click", listener)
    listener
  }

  def uninstall(
    listener: js.Function1[Event, Unit]
  ): Unit = {
    WindowWithRouteTo.routeTo = js.undefined
    dom.document.removeEventListener("click", listener)
  }

  @scala.annotation.tailrec
  private def findParent(nodeName: String, element: Node): js.UndefOr[Node] = {
    if (js.isUndefined(element) || element == null) {
      js.undefined
    } else {
      if (element.nodeName == nodeName) {
        element
      } else {
        findParent(nodeName, element.parentNode)
      }
    }
  }

}
