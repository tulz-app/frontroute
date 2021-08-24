package io.frontroute.site.examples.ex_signal

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString
import io.frontroute.LocationProvider
import io.laminext.AmAny
import io.laminext.AmendedHtmlTag
import org.scalajs.dom

object SignalExample
    extends CodeExample(
      id = "signal",
      title = "Signal",
      description = FileAsString("description.md")
    )((locationProvider: LocationProvider, a: AmendedHtmlTag[dom.html.Anchor, AmAny]) => {
      import io.frontroute._
      import com.raquo.laminar.api.L.{a => _, _}

      val mySignal = Var("Test")

      val (renders, route) = makeRoute[HtmlElement] { render =>
        /* <focus> */
        signal(mySignal.signal) { signalValue =>
          /* </focus> */
          concat(
            pathEnd {
              render {
                div(
                  div(cls := "text-2xl", "Index page."),
                  div(s"Signal value: $signalValue")
                )
              }
            },
            path("some-page") {
              render {
                div(
                  div(cls := "text-2xl", "Some page."),
                  div(s"Signal value: $signalValue")
                )
              }
            },
            extractUnmatchedPath { unmatched =>
              render {
                div(
                  div(cls := "text-2xl", "Not Found"),
                  div(unmatched.mkString("/", "/", ""))
                )
              }
            }
          )
        }
      }

      div(
        div(
          cls := "p-4 min-h-[300px]",
          child <-- renders.map(_.getOrElse(div("loading...")))
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
            cls := "flex flex-col",
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
        ),
        onMountCallback { ctx =>
          val _ = runRoute(route, locationProvider)(ctx.owner)
        }
      )
    })
