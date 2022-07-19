package io.frontroute.site
package examples
package ex_signal

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString
import io.laminext.AmAny
import io.laminext.AmendedHtmlTag
import org.scalajs.dom

object SignalExample
    extends CodeExample(
      id = "signal",
      title = "Signal",
      description = FileAsString("description.md")
    )((a: AmendedHtmlTag[dom.html.Anchor, AmAny]) => {
      import io.frontroute._
      import com.raquo.laminar.api.L.{a => _, _}

      val mySignal = Var("Test")

      val route =
        /* <focus> */
        signal(mySignal.signal) { signalValue =>
          /* </focus> */
          concat(
            pathEnd {
              complete {
                div(
                  div(cls := "text-2xl", "Index page."),
                  div(s"Signal value: $signalValue")
                )
              }
            },
            path("some-page") {
              complete {
                div(
                  div(cls := "text-2xl", "Some page."),
                  div(s"Signal value: $signalValue")
                )
              }
            },
            extractUnmatchedPath { unmatched =>
              complete {
                div(
                  div(cls := "text-2xl", "Not Found"),
                  div(unmatched.mkString("/", "/", ""))
                )
              }
            }
          )
        }

      div(
        div(
          cls := "p-4 min-h-[300px]",
          route
        ),
        div(
          cls := "bg-blue-900 -mx-4 -mb-4 p-2 space-y-2",
          div(
            cls := "font-semibold text-2xl text-blue-200",
            "Set signal"
          ),
          div(
            input(
              tpe         := "text",
              placeholder := "Input a value and hit enter...",
              onKeyDown.filter(_.key == "Enter").stopPropagation.mapToValue --> mySignal
            )
          ),
          div(
            cls := "font-semibold text-2xl text-blue-200",
            "Navigation"
          ),
          div(
            cls := "flex flex-col p-2",
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/",
              "➜ /"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/some-page",
              "➜ /some-page"
            )
          )
        )
      )
    })
