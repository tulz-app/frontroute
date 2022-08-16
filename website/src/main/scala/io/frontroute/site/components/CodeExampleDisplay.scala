package io.frontroute.site.components

import com.raquo.laminar.api.L._
import io.frontroute.BrowserNavigation
import io.frontroute.internal.UrlString
import io.laminext.syntax.core._
import io.laminext.syntax.tailwind._
import io.laminext.syntax.markdown._
import io.laminext.highlight.Highlight
import io.frontroute.site.examples.CodeExample
import io.frontroute.site.Site
import io.frontroute.site.Styles
import io.frontroute._
import io.frontroute.site.TemplateVars
import org.scalajs.dom
import org.scalajs.dom.HTMLIFrameElement
import org.scalajs.dom.html
import org.scalajs.dom.window

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

  def apply(example: CodeExample): Element = {
    val dimContext = storedBoolean("dim-context", initial = true)
    val hasContext = example.code.source.contains("/* <focus> */")

    val codeNode = (dim: Boolean) => {
      val theCode = pre(
        cls := "w-full text-sm",
        fixIndentation {
          example.code.source
        }
          .replaceAll(
            "\n[ ]*/\\* <focus> \\*/[ ]*\n",
            "\n/* <focus> */"
          )
          .replaceAll(
            "\n[ ]*/\\* </focus> \\*/[ ]*\n",
            "\n/* </focus> */"
          )
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
      cls := "flex-1 flex flex-col space-y-4",
      div(
        cls := "flex space-x-4 items-center",
        h1(
          cls := "font-display text-xl font-bold text-gray-900 tracking-wide",
          example.title
        ),
        (path(Set("live", "source", "description")) | pathEnd.mapTo("live")).signal { tab =>
          div(
            cls := "flex space-x-2",
            a(
              href := "live",
              cls  := "px-2 rounded",
              cls.toggle("bg-gray-500 text-gray-100 font-semibold") <-- tab.map(_ == "live"),
              cls.toggle("text-gray-700 font-semibold") <-- tab.map(_ != "live"),
              "Live Demo"
            ),
            a(
              href := "source",
              cls  := "px-2 rounded",
              cls.toggle("bg-gray-500 text-gray-100 font-semibold") <-- tab.map(_ == "source"),
              cls.toggle("text-gray-700 font-semibold") <-- tab.map(_ != "source"),
              "Source Code"
            ),
            a(
              href := "description",
              cls  := "px-2 text-lg font-semibold rounded",
              cls.toggle("bg-gray-500 text-gray-200") <-- tab.map(_ == "description"),
              cls.toggle("text-gray-800") <-- tab.map(_ != "description"),
              cls  := (if (example.description.trim.isEmpty) "hidden" else ""),
              "Description"
            )
          )
        }
      ),
      (path(Set("live", "source", "description")) | pathEnd.mapTo("live")).signal { tab =>
        div(
          cls := "flex-1 flex flex-col space-y-2",
          div(
            cls            := "flex-1 flex flex-col space-y-2",
            cls.toggle("hidden") <-- tab.map(_ != "source"),
            div(
              cls := "flex space-x-4 items-center",
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
                    "highlight relevant code"
                  )
                )
              }
            ),
            div(
              cls := "flex-1 shadow relative",
              child <-- Styles.highlightStyle.signal.combineWithFn(dimContext.signal) { (_, dim) =>
                codeNode(dim)
              }
            )
          ),
          div(
            cls            := "flex-1 flex flex-col",
            cls.toggle("hidden") <-- tab.map(_ != "live"),
            iframe(
              cls := "flex-1",
              onLoad --> { e =>
                val f = e.target.asInstanceOf[HTMLIFrameElement]
                f.style.height = (f.contentWindow.document.body.scrollHeight + 20).toString + "px"
              },
              src := Site.thisVersionHref(s"/example-frame/${example.id}")
            )
          ),
          div(
            cls            := "flex-1 flex flex-col prose max-w-none",
            cls.toggle("hidden") <-- tab.map(_ != "description"),
            unsafeMarkdown := TemplateVars(example.description),
            onMountCallback { ctx =>
              ctx.thisNode.ref.querySelectorAll("pre > code").foreach { codeElement =>
                Highlight.highlightElement(codeElement)
              }
            }
          )
        )
      }
    )
  }

  def frame(example: CodeExample): Element = {
    val currentUrl = windowEvents.onPopState
      .mapTo(window.location.toString).map { case UrlString(url) =>
        url.pathname
      }
      .startWith("/")

    val urlInput = input(
      value <-- currentUrl.map(path => "https://site.nowhere" + path),
      tpe         := "url",
      placeholder := "https://site.nowhere/path"
    )

    div(
      cls := "w-full h-full border-4 border-dashed border-blue-400 bg-blue-300 text-blue-900 rounded-lg p-6",
      div(
        cls := "-mx-4 -mt-4 p-4 w-full h-full bg-blue-500 flex space-x-1",
        urlInput.amend(
          cls := "flex-1",
          thisEvents(onKeyDown.filter(_.key == "Enter").preventDefault.stopPropagation).sample(urlInput.value) --> { case UrlString(url) =>
            BrowserNavigation.pushState(url = url.pathname)
          }
        ),
        button(
          cls := "btn-md-outline-white",
          "Go",
          thisEvents(onClick).sample(urlInput.value) --> { case UrlString(url) =>
            BrowserNavigation.pushState(url = url.pathname)
          }
        )
      ),
      onMountUnmountCallbackWithState(
        mount = ctx => render(ctx.thisNode.ref, example.code.value()),
        unmount = (_, root: Option[RootNode]) => root.foreach(_.unmount())
      )
    )
  }

  @scala.annotation.unused
  private def opaqueColor(color: String, opaque: Int, dim: Boolean): String = {
    if (opaque == 0 && dim) {
      if (color.startsWith("rgb(")) {
        color.replace("rgb(", "rgba(").replace(")", ", .5)")
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

    var childrenOpaque = opaque
    val newChildNodes  = element.childNodes.flatMap { child =>
      if (child.nodeName == "#text") {
        dom.console.log(child)
        val span = dom.document.createElement("span").asInstanceOf[html.Element]
        span.innerText = child.textContent
        span.style.color = opaqueColor(elementColor, childrenOpaque, dim)
        Some(span)
      } else {
        if (child.innerText.contains("<focus>")) {
          childrenOpaque += 1
          Some(child)
//          None
        } else if (child.innerText.contains("</focus>")) {
          childrenOpaque -= 1
          Some(child)
//          None
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
