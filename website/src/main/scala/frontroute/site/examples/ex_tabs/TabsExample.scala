package frontroute.site
package examples
package ex_tabs

import frontroute.site.examples.CodeExample
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
      import frontroute._

      import io.laminext.syntax.core._
      import com.raquo.laminar.api.L._

      val tabs = Seq(
        "tab-1" -> "Tab 1",
        "tab-2" -> "Tab 2",
      )

      routes(
        div(
          cls := "p-4 min-h-[300px]",
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
                      cls("border-blue-800 bg-blue-200 text-blue-800") <-- active,
                      cls("border-transparent text-blue-700") <-- !active,
                    )
                  },
                  /* </focus> */
                  tabLabel
                )
              }
            ),
            pathEnd {
              navigate("tab-1", replace = true)
            },
            /* <focus> */
            path(Set("tab-1", "tab-2")).signal { tab =>
              /* </focus> */
              div(
                div(
                  cls("hidden") <-- !tab.valueIs("tab-1"),
                  textArea("tab-1 text area", cls := "bg-blue-100 text-blue-500")
                ),
                div(
                  cls("hidden") <-- !tab.valueIs("tab-2"),
                  textArea("tab-2 text area", cls := "bg-blue-100 text-blue-500")
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
      )
    })
