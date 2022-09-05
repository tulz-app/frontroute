package io.frontroute.site
package examples
package ex_signal

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString

object SignalExample
    extends CodeExample(
      id = "signal",
      title = "Signal",
      description = FileAsString("description.md"),
      links = Seq(
        "/",
        "/some-page"
      )
    )(() => {
      import io.frontroute._
      import com.raquo.laminar.api.L._

      val mySignal = Var("Test")

      div(
        div(
          cls := "p-4 min-h-[300px]",
          /* <focus> */
          signal(mySignal.signal) { signalValue =>
            /* </focus> */
            firstMatch(
              pathEnd {
                div(
                  div(cls := "text-2xl", "Index page."),
                  div(s"Signal value: $signalValue")
                )
              },
              path("some-page") {
                div(
                  div(cls := "text-2xl", "Some page."),
                  div(s"Signal value: $signalValue")
                )
              },
              extractUnmatchedPath { unmatched =>
                div(
                  div(cls := "text-2xl", "Not Found"),
                  div(unmatched.mkString("/", "/", ""))
                )
              }
            )
            /* <focus> */
          }
          /* </focus> */
        ),
        div(
          cls := "bg-blue-900 -mx-6 p-2 space-y-2",
          div(
            cls := "font-semibold text-xl text-blue-200",
            "Set signal"
          ),
          div(
            input(
              tpe         := "text",
              placeholder := "Input a value and hit enter...",
              onKeyDown.filter(_.key == "Enter").stopPropagation.mapToValue --> mySignal
            )
          )
        )
      )
    })
