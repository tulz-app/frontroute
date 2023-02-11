package io.frontroute.site.examples.ex_matched_path

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString

object MatchedPathExample
    extends CodeExample(
      id = "matched-path",
      title = "Matched Path",
      description = FileAsString("description.md"),
      links = Seq(
        "/",
        "/tabs/tab-1",
        "/tabs/tab-2",
        "/tabs/tab-3",
        "/some-page"
      )
    )(() => {
      import io.frontroute._
      import io.laminext.syntax.core._

      import com.raquo.laminar.api.L._

      def ShowCurrentPath(label: String): Element =
        div(
          span(
            cls := "bg-yellow-200 text-yellow-900 rounded-sm space-x-2 text-sm px-2 font-mono",
            span(label),
            span(
              /* <focus> */
              withMatchedPath { path =>
                child.text <-- path.map(s => s"'${s.mkString("/", "/", "")}'")
              }
              /* </focus> */
            )
          )
        )

      val tabs = Seq(
        "tab-1" -> "Tab 1",
        "tab-2" -> "Tab 2",
      )

      def MyComponent(): Element =
        div(
          cls := "space-y-2",
          div(
            cls := "flex space-x-2",
            tabs.map { case (path, tabLabel) =>
              a(
                /* <focus> */
                relativeHref(path),
                /* </focus> */
                cls := "text-xl px-4 py-1 rounded border-b-2",
                /* <focus> */
                navMod { active =>
                  Seq(
                    cls.toggle("border-blue-800 bg-blue-200 text-blue-800") <-- active,
                    cls.toggle("border-transparent text-blue-700") <-- !active,
                  )
                },
                /* </focus> */
                tabLabel,
              )
            },
            a(
              relativeHref("tab-3"),
              cls := "text-xl px-4 py-1 rounded border-b-2",
              navMod { active =>
                Seq(
                  cls.toggle("border-blue-800 bg-blue-200 text-blue-800") <-- active,
                  cls.toggle("border-transparent text-blue-700") <-- !active,
                )
              },
              "Tab 3",
            )
          ),
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
            path("tab-3") {
              div(
                cls := "bg-blue-100 text-blue-600 p-4",
                div("Content three"),
                ShowCurrentPath("Inside tab-3:"),
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
