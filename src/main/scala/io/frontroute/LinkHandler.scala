package io.frontroute

import org.scalajs.dom
import org.scalajs.dom.ext._
import org.scalajs.dom.raw._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

object LinkHandler {

  @js.native
  @JSGlobal("window")
  private object WindowWithRouteTo extends js.Object {
    var routeTo: js.UndefOr[js.Function1[String, Unit]] = js.native
  }

  private val clickListener: js.Function1[Event, Boolean] = event => {
    findParent("a", event.currentTarget.asInstanceOf[Node]).fold(true) { element =>
      val anchor = element.asInstanceOf[HTMLAnchorElement]
      val rel    = anchor.rel
      if (js.isUndefined(rel) || rel == null || rel == "") {
        BrowserNavigation.pushState(url = anchor.href)
        false
      } else {
        rel match {
          case "external" =>
            event.preventDefault()
            dom.window.open(anchor.href)
            false
          case _ =>
            true
        }
      }
    }
  }

  private val observer = new MutationObserver((records, _) => {
    records.foreach { record =>
      record.addedNodes.foreach { node =>
        node.addEventListener("click", clickListener)
      }
      record.removedNodes.foreach { node =>
        node.removeEventListener("click", clickListener)
      }
    }
  })

  def uninstall(): Unit = {
    observer.disconnect()
    WindowWithRouteTo.routeTo = js.undefined
  }

  private val routeTo: js.Function1[String, Unit] = (path: String) => BrowserNavigation.pushState(null, null, path)

  def install(): Unit = {
    WindowWithRouteTo.routeTo = routeTo
    observer.observe(dom.document, js.Dynamic.literal(childList = true).asInstanceOf[MutationObserverInit])
    dom.document.querySelectorAll("a").foreach(_.addEventListener("click", clickListener))
  }

  @scala.annotation.tailrec
  private def findParent(tagName: String, element: Node): js.UndefOr[Node] = {
    if (js.isUndefined(element) || element == null) {
      js.undefined
    } else {
      val tagMatched =
        element.nodeName
          .asInstanceOf[js.UndefOr[String]]
          .orElse(
            element.asInstanceOf[Element].tagName.asInstanceOf[js.UndefOr[String]]
          )
          .map(_.toLowerCase())
          .contains(tagName.toLowerCase())
      if (tagMatched) {
        element
      } else {
        findParent(tagName, element.parentNode)
      }
    }
  }

}
