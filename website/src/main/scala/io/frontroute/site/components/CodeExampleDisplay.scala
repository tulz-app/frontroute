package io.frontroute.site.components

import com.raquo.laminar.api.L._
import io.frontroute.BrowserNavigation
import io.frontroute.internal.UrlString
import io.laminext.syntax.core._
import io.laminext.highlight.Highlight
import io.frontroute.site.examples.CodeExample
import io.frontroute.site.Site
import io.frontroute.site.Styles
import io.frontroute._
import io.frontroute.site.TemplateVars
import io.laminext.markdown.markedjs.Marked
import org.scalajs.dom
import org.scalajs.dom.HTMLIFrameElement
import org.scalajs.dom.Location
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
        cls := "w-full text-sm language-scala",
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

    val tabs = Seq(
      "live"   -> "Live Demo",
      "source" -> "Source Code",
    ) ++ Seq("description" -> "Description").filter(_ => example.description.trim.nonEmpty)

    div(
      cls := "flex-1 flex flex-col space-y-4",
      div(
        cls := "flex space-x-4 items-center",
        h1(
          cls := "font-display text-xl font-bold text-gray-900 tracking-wide",
          example.title
        ),
        div(
          cls := "flex space-x-2",
          tabs.map { case (path, tabLabel) =>
            a(
              href := path,
              cls  := "px-2 rounded",
              navMod { active =>
                Seq(
                  cls.toggle("bg-gray-500 text-gray-100 font-semibold") <-- active,
                  cls.toggle("text-gray-700 font-semibold") <-- !active,
                )
              },
              tabLabel
            )
          }
        )
      ),
      (path(Set("live", "source", "description")) | pathEnd.mapTo("live")).signal { tab =>
        div(
          cls := "flex-1 flex flex-col space-y-2",
          div(
            cls := "flex-1 flex flex-col space-y-2",
            cls.toggle("hidden") <-- tab.map(_ != "source"),
            div(
              cls := "flex space-x-4 items-center",
              when(hasContext) {
                label(
                  cls := "btn-sm-text-blue flex-shrink-0 flex space-x-1 items-center cursor-pointer",
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
            cls := "flex-1 flex flex-col",
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
            cls := "flex-1 flex flex-col prose max-w-none",
            cls.toggle("hidden") <-- tab.map(_ != "description"),
            new Modifier[HtmlElement] {
              override def apply(element: HtmlElement): Unit = element.ref.innerHTML = Marked
                .parse(TemplateVars(example.description)).replace(
                  """<a href="/""",
                  s"""<a href="${Site.thisVersionPrefix}"""
                )
            },
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
    def pathAndSearch(url: Location): String =
      url.pathname + (if (url.search != null && url.search.nonEmpty) url.search else "")

    val currentUrl = windowEvents(_.onPopState)
      .mapTo(window.location.toString).map { case UrlString(url) => pathAndSearch(url) }
      .startWith("/")

    val urlInput = input(
      value <-- currentUrl.map(path => "https://site.nowhere" + path),
      tpe         := "url",
      placeholder := "https://site.nowhere/path"
    )

    div(
      cls := "border-4 border-dashed border-blue-400 bg-blue-300 text-blue-900 rounded-lg p-6",
      div(
        cls := "-mx-6 -mt-6 p-2 rounded-t-lg bg-blue-500 flex space-x-1",
        urlInput.amend(
          cls := "flex-1",
          thisEvents(onKeyDown.filter(_.key == "Enter").preventDefault.stopPropagation).sample(urlInput.value) --> { case UrlString(url) =>
            BrowserNavigation.pushState(url = pathAndSearch(url))
          }
        ),
        button(
          cls := "btn-md-outline-white",
          "Go",
          thisEvents(onClick).sample(urlInput.value) --> { case UrlString(url) =>
            BrowserNavigation.pushState(url = pathAndSearch(url))
          }
        )
      ),
      example.code.value(),
      div(
        cls := "rounded-b-lg bg-blue-900 -mx-6 -mb-6 p-2",
        div(
          cls := "font-semibold text-xl text-blue-200",
          "Navigation"
        ),
        div(
          cls := "flex flex-col p-2",
          example.links.map { path =>
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := path,
              s"➜ $path"
            )
          }
        )
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
        val span = dom.document.createElement("span").asInstanceOf[html.Element]
        span.innerText = child.textContent
        span.style.color = opaqueColor(elementColor, childrenOpaque, dim)
        Some(span)
      } else {
        if (child.innerText.contains("<focus>")) {
          childrenOpaque += 1
          None
        } else if (child.innerText.contains("</focus>")) {
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
        if (child.innerText.contains("<focus>") || child.innerText.contains("</focus>")) {
          child.asInstanceOf[html.Element].style.display = "none"
        } else {
          hideFocusMarkers(child.asInstanceOf[html.Element])
        }
      }
    }

}
