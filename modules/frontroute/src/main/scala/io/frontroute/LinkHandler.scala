package io.frontroute

import com.raquo.laminar.api.L._
import io.frontroute.internal.UrlString
import org.scalajs.dom
import scala.scalajs.js

object LinkHandler {

  private val clickListener: js.Function1[dom.Event, Unit] = event => {
    findParent("A", event.target.asInstanceOf[dom.Node]).foreach { aParent =>
      val anchor     = aParent.asInstanceOf[dom.HTMLAnchorElement]
      val rel        = anchor.rel
      val href       = anchor.href
      val sameOrigin =
        href.startsWith("/") ||
          !href.startsWith("http://") && !href.startsWith("https://") ||
          dom.window.location.origin.exists(origin => href.startsWith(origin))

      if (sameOrigin && (js.isUndefined(rel) || rel == null || rel == "" || rel == "replace")) {
        event.preventDefault()
        val shouldPush = UrlString.unapply(anchor.href).fold(true) { location =>
          location.pathname != dom.window.location.pathname ||
          location.search != dom.window.location.search ||
          location.hash != dom.window.location.hash
        }
        if (shouldPush) {
          if (rel == "replace") {
            BrowserNavigation.replaceState(url = anchor.href)
          } else {
            BrowserNavigation.pushState(url = anchor.href)
          }
        }
      } else if (rel == "external") {
        event.preventDefault()
        dom.window.open(anchor.href)
      }
    }
  }

  val bind: Modifier[Element] =
    onMountUnmountCallback(
      { ctx =>
        ctx.thisNode.ref.addEventListener("click", clickListener)
      },
      { el =>
        el.ref.removeEventListener("click", clickListener)
      }
    )

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
