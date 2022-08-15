package io.frontroute.site
package examples
package ex_tabs

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString

object TabsExample
    extends CodeExample(
      id = "tabs",
      title = "Tabs",
      description = FileAsString("description.md")
    )(() => {
      import io.frontroute._

      import io.laminext.syntax.core._
      import com.raquo.laminar.api.L._

      val route =
        concat(
          /* <focus> */
          (pathEnd.mapTo("tab-1") | path(Set("tab-1", "tab-2"))).signal { tab =>
            /* </focus> */
            div(
              cls := "space-y-2",
              div(
                cls := "flex space-x-2",
                a(
                  href := "/tab-1",
                  "Tab 1",
                  cls  := "text-xl p-1 rounded",
                  cls.toggle("bg-blue-400 text-blue-100") <-- tab.valueIs("tab-1")
                ),
                a(
                  href := "/tab-2",
                  "Tab 2",
                  cls  := "text-xl p-1 rounded",
                  cls.toggle("bg-blue-400 text-blue-100") <-- tab.valueIs("tab-2")
                )
              ),
              div(
                div(
                  cls.toggle("hidden") <-- !tab.valueIs("tab-1"),
                  textArea("tab-1 text area", cls := "bg-blue-100 text-blue-500")
                ),
                div(
                  cls.toggle("hidden") <-- !tab.valueIs("tab-2"),
                  textArea("tab-2 text area", cls := "bg-blue-100 text-blue-500")
                )
              )
            )
          },
          extractUnmatchedPath { unmatched =>
            div(
              div(cls := "text-2xl", "Not Found"),
              div(unmatched.mkString("/", "/", ""))
            )
          }
        )

      div(
        div(
          cls := "p-4 min-h-[300px]",
          route
        ),
        div(
          cls := "bg-blue-900 -mx-4 -mb-4 p-2 space-y-2",
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
              href := "/tab-1",
              "➜ /tab-1"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/tab-2",
              "➜ /tab-2"
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
