package frontroute

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveElement
import frontroute.internal.LocationState
import frontroute.internal.UrlString
import org.scalajs.dom
import org.scalajs.dom.Element as DOMElement
import org.scalajs.dom.HTMLAnchorElement
import org.scalajs.dom.HTMLElement
import org.scalajs.dom.HTMLIFrameElement
import org.scalajs.dom.HTMLImageElement
import org.scalajs.dom.HTMLLinkElement
import org.scalajs.dom.HTMLScriptElement
import org.scalajs.dom.MutationObserver
import org.scalajs.dom.MutationObserverInit
import org.scalajs.dom.MutationRecord
import org.scalajs.dom.Node as HTMLNode

import scala.scalajs.js

private[frontroute] object HrefHandler {

  @js.native
  private trait HTMLElementWithState extends js.Object {
    var frontroute_href_updated: js.UndefOr[Boolean]
    var frontroute_href_subscription: js.UndefOr[Subscription]
  }

  def install(ctx: MountContext[ReactiveElement.Base], options: FrontrouteOptions): MutationObserver = {
    implicit val owner: Owner = ctx.owner

    processElementChildren(ctx.thisNode.ref)(options)

    val callback: js.Function2[js.Array[MutationRecord], MutationObserver, ?] = (records, _) => {
      records.foreach { rec =>
        rec.`type` match {
          case "attributes" =>
            processElement(rec.target)(options)
          case "childList"  =>
            rec.addedNodes.foreach(processElement(_)(options))
            rec.removedNodes.foreach(processRemovedElement(_)(options))
        }
      }
    }

    val observer = MutationObserver(callback)

    observer.observe(
      ctx.thisNode.ref,
      new MutationObserverInit {
        childList = true
        attributes = true
        subtree = true
        attributeFilter = js.Array("href", "src")
      }
    )
    observer
  }

  private def processElement(element: HTMLNode)(options: FrontrouteOptions)(implicit owner: Owner): Unit = element match {
    case anchor: HTMLAnchorElement => if (options.processAnchorHref) processElementWithHref(anchor)
    case image: HTMLImageElement   => if (options.processImageSrc) processElementWithSrc(image)
    case iframe: HTMLIFrameElement => if (options.processIframeSrc) processElementWithSrc(iframe)
    case link: HTMLLinkElement     => if (options.processLinkHref) processElementWithHref(link)
    case script: HTMLScriptElement => if (options.processScriptSrc) processElementWithSrc(script)
    case element: HTMLElement      => processElementChildren(element)(options)
    case _                         => // noop
  }

  private def processElementChildren(element: DOMElement)(options: FrontrouteOptions)(implicit owner: Owner): Unit = {
    if (options.processAnchorHref) {
      element.getElementsByTagName("a").foreach {
        case anchor: HTMLAnchorElement =>
          processElementWithHref(anchor)
        case _                         => // noop
      }
    }

    if (options.processImageSrc) {
      element.getElementsByTagName("img").foreach {
        case image: HTMLImageElement =>
          processElementWithSrc(image)
        case _                       => // noop
      }
    }

    if (options.processIframeSrc) {
      element.getElementsByTagName("iframe").foreach {
        case iframe: HTMLIFrameElement =>
          processElementWithSrc(iframe)
        case _                         => // noop
      }
    }

    if (options.processScriptSrc) {
      element.getElementsByTagName("script").foreach {
        case iframe: HTMLScriptElement =>
          processElementWithSrc(iframe)
        case _                         => // noop
      }
    }

    if (options.processLinkHref) {
      element.getElementsByTagName("link").foreach {
        case iframe: HTMLLinkElement =>
          processElementWithHref(iframe)
        case _                       => // noop
      }
    }
  }

  private def processRemovedElement(
    element: HTMLNode,
  )(options: FrontrouteOptions): Unit = element match {
    case element: HTMLAnchorElement => stopSubscriptionIfAny(element)
    case element: HTMLImageElement  => stopSubscriptionIfAny(element)
    case element: HTMLIFrameElement => stopSubscriptionIfAny(element)
    case element: HTMLScriptElement => stopSubscriptionIfAny(element)
    case element: HTMLLinkElement   => stopSubscriptionIfAny(element)
    case element: HTMLElement       =>
      if (options.processAnchorHref) element.getElementsByTagName("a").foreach(stopSubscriptionIfAny)
      if (options.processImageSrc) element.getElementsByTagName("img").foreach(stopSubscriptionIfAny)
      if (options.processIframeSrc) element.getElementsByTagName("iframe").foreach(stopSubscriptionIfAny)
      if (options.processScriptSrc) element.getElementsByTagName("script").foreach(stopSubscriptionIfAny)
      if (options.processLinkHref) element.getElementsByTagName("link").foreach(stopSubscriptionIfAny)
    case _                          => // noop
  }

  private def stopSubscriptionIfAny(
    element: HTMLNode
  ): Unit = {
    val elementWithState = element.asInstanceOf[HTMLElementWithState]
    elementWithState.frontroute_href_subscription.foreach { oldSubscription =>
      if (!oldSubscription.isKilled) {
        oldSubscription.kill()
      }
    }
    elementWithState.frontroute_href_subscription = js.undefined
  }

  private def processElementWithHref(
    element: HTMLAnchorElement | HTMLLinkElement,
  )(implicit owner: Owner): Unit = {
    val href         = element.getAttribute("href")
    val shouldIgnore = Option(element.getAttribute("data-fr-rewrite")).contains("ignore")

    val elementWithState = element.asInstanceOf[HTMLElementWithState]

    if (elementWithState.frontroute_href_updated.contains(true)) {
      elementWithState.frontroute_href_updated = false
      return
    }

    if (href == null || shouldIgnore) { return }

    LocationState.closest(element).foreach { locationState =>
      stopSubscriptionIfAny(element)

      val baseName = locationState.baseName
      if (baseName == "" || !href.startsWith(baseName)) {
        if (href.startsWith("/") && !href.startsWith("//")) {
          elementWithState.frontroute_href_updated = true
          element.setAttribute("href", baseName + href)
        } else if (!href.contains("://")) {
          // relative href

          def update(matched: List[String]): Unit = {
            val UrlString(url) = href
            elementWithState.frontroute_href_updated = true
            val updatedHref    = makeRelative(matched, href.takeWhile(_ != '?'), url.search, locationState.baseName)
            element.setAttribute("href", updatedHref)
          }

          update(locationState.consumed.now())
          val subscription = locationState.consumed.updates.foreach(update)
          elementWithState.frontroute_href_subscription = subscription
        }
      }
    }
  }

  private def processElementWithSrc(
    element: HTMLImageElement | HTMLIFrameElement | HTMLScriptElement,
  )(implicit owner: Owner): Unit = {
    val src              = element.getAttribute("src")
    val shouldIgnore     = element.dataset.get("fr-rewrite").contains("ignore")
    val elementWithState = element.asInstanceOf[HTMLElementWithState]

    if (elementWithState.frontroute_href_updated.contains(true)) {
      elementWithState.frontroute_href_updated = false
      return
    }

    if (src == null || shouldIgnore) {
      return
    }

    LocationState.closest(element).foreach { locationState =>
      stopSubscriptionIfAny(element)

      val baseName = locationState.baseName
      if (!src.startsWith(baseName)) {
        if (src.startsWith("/") && !src.startsWith("//")) {
          elementWithState.frontroute_href_updated = true
          element.setAttribute("src", baseName + src)
        } else if (!src.contains("://")) {
          // relative src

          def update(matched: List[String]): Unit = {
            val UrlString(url) = src
            elementWithState.frontroute_href_updated = true
            val updatedHref    = makeRelative(matched, src.takeWhile(_ != '?'), url.search, locationState.baseName)
            element.setAttribute("src", updatedHref)
          }

          update(locationState.consumed.now())
          val subscription = locationState.consumed.updates.foreach(update)
          elementWithState.frontroute_href_subscription = subscription
        }
      }
    }
  }

}
