package io.frontroute.site.examples.ex_extract_consumed

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString

object ExtractConsumedExample
    extends CodeExample(
      id = "extract-consumed",
      title = "Extract Consumed Path",
      description = FileAsString("description.md"),
      links = Seq(
        "/",
        "/tabs/tab-1",
        "/tabs/tab-2",
        "/some-page"
      )
    )(() => {
      import io.frontroute._

      import com.raquo.laminar.api.L._

      def ShowCurrentPath(label: String): Element =
        div(
          span(
            cls := "bg-yellow-200 text-yellow-900 rounded-sm space-x-2 text-sm px-2 font-mono",
            span(label),
            /* <focus> */
            extractConsumed.signal { path =>
              span(
                child.text <-- path.map(s => s"'${s.mkString("/", "/", "")}'")
              )
            }
            /* </focus> */
          )
        )

      def MyComponent(): Element =
        div(
          cls := "space-y-2",
          path(segment).signal { tab =>
            div(
              cls := "flex space-x-2",
              a(
                href := "tab-1",
                cls  := "text-xl px-4 py-1 rounded border-b-2",
                cls.toggle("border-blue-800 bg-blue-200 text-blue-800") <-- tab.map(_ == "tab-1"),
                cls.toggle("border-transparent text-blue-700") <-- tab.map(_ != "tab-1"),
                "Tab 1",
              ),
              a(
                href := "tab-2",
                cls  := "text-xl px-4 py-1 rounded border-b-2",
                cls.toggle("border-blue-800 bg-blue-200 text-blue-800") <-- tab.map(_ == "tab-2"),
                cls.toggle("border-transparent text-blue-700") <-- tab.map(_ != "tab-2"),
                "Tab 2",
              )
            )
          },
          div(
            ShowCurrentPath("Inside component:"),
            path("tab-1") {
              div(
                cls := "bg-blue-100 text-blue-600 p-4",
                div("Content one."),
                ShowCurrentPath("Inside tab-1:"),
              )
            },
            path("tab-2") {
              div(
                cls := "bg-blue-100 text-blue-600 p-4",
                div("Content two"),
                ShowCurrentPath("Inside tab-2:"),
              )
            },
          )
        )

      div(
        div(
          cls := "p-4 min-h-[300px]",
          pathEnd {
            div(
              cls := "text-2xl",
              div(
                "Index page."
              ),
              ShowCurrentPath("Inside index:")
            )
          },
          pathPrefix("tabs") {
            div(
              MyComponent()
            )
          },
          (noneMatched & extractUnmatchedPath) { unmatched =>
            div(
              div(cls := "text-2xl", "Not Found"),
              div(unmatched.mkString("/", "/", ""))
            )
          }
        )
      )
    })
