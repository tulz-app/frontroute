package io.frontroute.site.components

import com.raquo.laminar.api.L._
import io.frontroute.LocationProvider
import io.laminext.syntax.core._
import io.laminext.syntax.tailwind._
import io.laminext.syntax.markdown._
import io.laminext.highlight.Highlight
import io.frontroute.site.examples.CodeExample
import io.laminext.tailwind.theme
import io.frontroute.site.Styles
import io.frontroute.site.TemplateVars
import io.laminext.util.UrlString
import org.scalajs.dom
import org.scalajs.dom.html
import scala.scalajs.js

object CodeExampleDisplay {

  private def fixIndentation(src: String): String = {
    val lines =
      src
        .split('\n')
        .drop(1)
        .dropRight(1)

    val minIndent =
      lines
        .filterNot(_.trim.isEmpty)
        .map(_.takeWhile(_.isWhitespace))
        .map(_.length)
        .minOption
        .getOrElse(0)

    lines.map(_.drop(minIndent)).mkString("\n")
  }

  private val collapseTransition = theme.Theme.current.transition.resize.customize(
    hidden = _ :+ "max-h-32",
    showing = _ :+ "max-h-[400px]",
    enterFrom = _ :+ "max-h-32",
    enterTo = _ :+ "max-h-[400px]",
    leaveFrom = _ :+ "max-h-[400px]",
    leaveTo = _ :+ "max-h-32"
  )

  def apply(example: CodeExample): Element = {
    val sourceCollapsed  = storedBoolean(example.id, initial = true)
    val dimContext       = storedBoolean("dim-context", initial = true)
    val hasContext       = example.code.source.contains("/* <focus> */")
    val locations        = new EventBus[String]
    val currentLocation  = locations.events.toSignal("")
    val locationProvider = LocationProvider.custom(locations.events)

    val urlInput = input(
      value <-- locations,
      tpe         := "url",
      placeholder := "https://site.nowhere/path"
    )

    val amendedA = a.amend[AmAny](
      thisEvents(onClick.preventDefault.stopPropagation)
        .withCurrentValueOf(currentLocation)
        .map { case (e, current) =>
          val href            = e.target.getAttribute("href")
          val anchorLocation  = UrlString.parse(href)
          val currentLocation = UrlString.parse(current)
          if (!href.contains("://")) {
            currentLocation.search = anchorLocation.search
            if (href.startsWith("/")) {
              currentLocation.pathname = anchorLocation.pathname
            } else {
              currentLocation.pathname = currentLocation.pathname.reverse.dropWhile(_ != '/').reverse + href
            }
            val search = currentLocation.search
            s"${currentLocation.protocol}//${currentLocation.hostname}${currentLocation.pathname}${search}"
          } else {
            anchorLocation.href
          }
        } --> locations
    )

    val codeNode = (dim: Boolean) => {
      val theCode = pre(
        cls := "w-full text-sm",
        fixIndentation {
          example.code.source
            .replace("import com.raquo.laminar.api.L.{a => _, _}", "import com.raquo.laminar.api.L._")
            .replace(
              """(locationProvider: LocationProvider) =>
                |      (a: AmendedHtmlTag[dom.html.Anchor, AmAny]) =>
                |        useLocationProvider(locationProvider) { implicit locationProvider =>""".stripMargin,
              ""
            )
        }
      )
      div(
        theCode,
        onMountCallback { ctx =>
          Highlight.highlightElement(ctx.thisNode.ref.childNodes.head)
          hideFocusMarkers(ctx.thisNode.ref.childNodes.head.asInstanceOf[html.Element])
          if (hasContext) {
            val _ = js.timers.setTimeout(100) {
              val _ = js.timers.setTimeout(0) {
                val updatedNode = setOpacityRecursively(theCode.ref, 0, dim)
                val _           = ctx.thisNode.ref.replaceChild(updatedNode, ctx.thisNode.ref.childNodes.head)
              }
            }
          }
        }
      )
    }

    div(
      cls := "flex flex-col space-y-4 mb-20",
      div(
        h1(
          cls := "font-display text-3xl font-bold text-gray-900 tracking-wider",
          example.title
        )
      ),
      div(
        cls            := "prose max-w-none",
        unsafeMarkdown := TemplateVars(example.description),
        onMountCallback { ctx =>
          ctx.thisNode.ref.querySelectorAll("pre > code").foreach { codeElement =>
            Highlight.highlightElement(codeElement)
          }
        }
      ),
      div(
        cls            := "space-y-2",
        div(
          cls := "flex space-x-4 items-center",
          h2(
            cls := "flex-1 text-xl font-semibold text-gray-900",
            "Source code:"
          ),
          when(hasContext) {
            label.btn.sm.text.blue(
              cls := "flex-shrink-0 flex space-x-1 items-center cursor-pointer",
              input(
                tpe := "checkbox",
                checked <-- dimContext.signal,
                inContext { el =>
                  el.events(onClick) --> { _ =>
                    dimContext.set(el.ref.checked)
                  }
                }
              ),
              span(
                "dim context"
              )
            )
          },
          span(
            cls := "flex-shrink-0",
            button.btn.sm.text.blue(
              cls := "w-20 justify-center",
              child.text <-- sourceCollapsed.signal.switch("expand", "collapse"),
              onClick --> sourceCollapsed.toggleObserver
            )
          )
        ),
        div(
          cls := "overflow-hidden shadow relative",
          div(
            cls := "overflow-auto",
            TW.transition(show = !sourceCollapsed.signal, collapseTransition),
            child <-- Styles.highlightStyle.signal.combineWithFn(dimContext.signal) { (_, dim) =>
              codeNode(dim)
            }
          ),
          div(
            cls := "p-2 absolute left-0 right-0 bottom-0 bg-gradient-to-b from-gray-500 to-gray-600 opacity-75",
            button(
              cls := "w-full h-full text-center p-1 focus:outline-none focus:ring focus:ring-gray-200 text-gray-50 font-semibold",
              onClick.mapToUnit --> sourceCollapsed.toggleObserver,
              "expand"
            )
          ).visibleIf(sourceCollapsed.signal),
          div(
            cls := "p-2 bg-gradient-to-b from-gray-500 to-gray-600 opacity-75",
            button(
              cls := "w-full h-full text-center p-1 focus:outline-none focus:ring focus:ring-gray-200 text-gray-200 font-semibold",
              onClick.mapToUnit --> sourceCollapsed.toggleObserver,
              "collapse"
            )
          ).hiddenIf(sourceCollapsed.signal)
        )
      ),
      div(
        cls            := "space-y-2",
        h2(
          cls := "text-xl font-semibold text-gray-900",
          "Live demo:"
        ),
        div(
          cls := "border-4 border-dashed border-blue-400 bg-blue-300 text-blue-900 rounded-lg -m-2 p-4",
          div(
            cls := "-mx-4 -mt-4 p-4 bg-blue-500 flex space-x-1",
            urlInput.amend(
              cls := "flex-1",
              thisEvents(onKeyDown.filter(_.key == "Enter").preventDefault.stopPropagation).sample(urlInput.value) --> locations
            ),
            button(
              cls := "btn-md-outline-white",
              "Go",
              thisEvents(onClick).sample(urlInput.value) --> locations
            )
          ),
          onMountUnmountCallbackWithState(
            mount = ctx => render(ctx.thisNode.ref, example.code.value(locationProvider)(amendedA)),
            unmount = (_, root: Option[RootNode]) => root.foreach(_.unmount())
          )
        ),
        onMountCallback { _ =>
          locations.emit("https://site.nowhere/")
        }
      )
    )
  }

  private def opaqueColor(color: String, opaque: Int, dim: Boolean): String = {
    if (opaque == 0 && dim) {
      if (color.startsWith("rgb(")) {
        color.replace("rgb(", "rgba(").replace(")", ", .4)")
      } else {
        color
      }
    } else {
      color
    }
  }

  private def setOpacityRecursively(element: html.Element, opaque: Int, dim: Boolean): dom.Node = {
    val elementColor = dom.window.getComputedStyle(element).color
    val newElement   = element.cloneNode(false).asInstanceOf[html.Element]
    if (opaque == 0) {
      newElement.style.color = opaqueColor(elementColor, opaque, dim)
    }

    var childrenOpaque = opaque
    val newChildNodes  = element.childNodes.flatMap { child =>
      if (child.nodeName == "#text") {
        val span = dom.document.createElement("span").asInstanceOf[html.Element]
        span.innerText = child.textContent
        span.style.color = opaqueColor(elementColor, childrenOpaque, dim)
        Some(span)
      } else {
        if (child.innerText == "/* <focus> */") {
          childrenOpaque += 1
          None
        } else if (child.innerText == "/* </focus> */") {
          childrenOpaque -= 1
          None
        } else {
          Some(setOpacityRecursively(child.asInstanceOf[html.Element], childrenOpaque, dim))
        }
      }
    }
    newChildNodes.foreach(newElement.appendChild)
    newElement
  }

  private def hideFocusMarkers(element: html.Element): Unit =
    element.childNodes.foreach { child =>
      if (child.nodeName != "#text") {
        if (child.innerText == "/* <focus> */" || child.innerText == "/* </focus> */") {
          child.asInstanceOf[html.Element].style.display = "none"
        } else {
          hideFocusMarkers(child.asInstanceOf[html.Element])
        }
      }
    }

}
