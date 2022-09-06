package io.frontroute.site
package examples
package ex_tabs

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString

object TabsExample
    extends CodeExample(
      id = "tabs",
      title = "Tabs",
      description = FileAsString("description.md"),
      links = Seq(
        "/",
        "/tab-1",
        "/tab-2",
        "/some-page"
      )
    )(() => {
      import io.frontroute._

      import io.laminext.syntax.core._
      import com.raquo.laminar.api.L._

      div(
        div(
          cls := "p-4 min-h-[300px]",
          /* <focus> */
          (pathEnd.mapTo("tab-1") | path(Set("tab-1", "tab-2"))).signal { tab =>
            /* </focus> */
            div(
              cls := "space-y-2",
              div(
                cls := "flex space-x-2",
                a(
                  href := "/tab-1",
                  cls  := "text-xl px-4 py-1 rounded border-b-2",
                  /* <focus> */
                  cls.toggle("border-blue-800 bg-blue-200 text-blue-800") <-- tab.map(_ == "tab-1"),
                  cls.toggle("border-transparent text-blue-700") <-- tab.map(_ != "tab-1"),
                  /* </focus> */
                  "Tab 1"
                ),
                a(
                  href := "/tab-2",
                  cls  := "text-xl px-4 py-1 rounded border-b-2",
                  /* <focus> */
                  cls.toggle("border-blue-800 bg-blue-200 text-blue-800") <-- tab.map(_ == "tab-2"),
                  cls.toggle("border-transparent text-blue-700") <-- tab.map(_ != "tab-2"),
                  /* </focus> */
                  "Tab 2"
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
            /* <focus> */
          },
          /* </focus> */
          (noneMatched & extractUnmatchedPath) { unmatched =>
            div(
              div(cls := "text-2xl", "Not Found"),
              div(unmatched.mkString("/", "/", ""))
            )
          }
        )
      )
    })
