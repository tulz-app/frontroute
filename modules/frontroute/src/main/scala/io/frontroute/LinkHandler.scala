package io.frontroute

import io.frontroute.internal.UrlString
import org.scalajs.dom
import scala.scalajs.js

object LinkHandler {

  private def clickListener(defaultTitle: String): js.Function1[dom.Event, Unit] = event => {
    findParent("A", event.target.asInstanceOf[dom.Node]).foreach { aParent =>
      val anchor      = aParent.asInstanceOf[dom.HTMLAnchorElement]
      val rel         = anchor.rel
      val href        = anchor.href
      val title       = anchor.dataset.get("title")
      val description = anchor.dataset.get("description")
      val keywords    = anchor.dataset.get("keywords")
      val sameOrigin  =
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
          val pageMeta = PageMeta(
            title = title.getOrElse(defaultTitle),
            description = description,
            keywords = keywords
          )
          BrowserNavigation.pushState(url = anchor.href, meta = pageMeta)
        }
      } else if (rel == "external") {
        event.preventDefault()
        dom.window.open(anchor.href)
      }
    }
  }

  def install(
    defaultTitle: String = ""
  ): js.Function1[dom.Event, Unit] = {
    val listener = clickListener(defaultTitle)
    dom.document.addEventListener("click", listener)
    listener
  }

  def uninstall(
    listener: js.Function1[dom.Event, Unit]
  ): Unit = {
    dom.document.removeEventListener("click", listener)
  }

  @scala.annotation.tailrec
  private def findParent(nodeName: String, element: dom.Node): js.UndefOr[dom.Node] = {
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
