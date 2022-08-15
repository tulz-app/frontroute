package io.frontroute.site.examples.ex_nested

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString

object NestedExample
    extends CodeExample(
      id = "nested",
      title = "Nested",
      description = FileAsString("description.md")
    )(() => {
      import io.frontroute._

      import com.raquo.laminar.api.L._

      def MyComponent(): Element =
        div(
          cls := "space-y-2",
          div(
            cls := "flex space-x-2",
            a(
              href := "tab-1",
              cls  := "text-xl p-1 rounded",
              navMod { active =>
                cls.toggle("bg-blue-400 text-blue-100") <-- active
              },
              "Tab 1"
            ),
            a(
              href := "tab-2",
              cls  := "text-xl p-1 rounded",
              navMod { active =>
                cls.toggle("bg-blue-400 text-blue-100") <-- active
              },
              "Tab 2"
            )
          ),
          div(
            /* <focus> */
            path("tab-1") {
              /* </focus> */
              div("Content one.", cls := "bg-blue-100 text-blue-500 p-4")
            },
            /* <focus> */
            path("tab-2") {
              /* </focus> */
              div("Content two", cls := "bg-blue-100 text-blue-500 p-4")
            }
          )
        )

      div(
        div(
          cls := "p-4 min-h-[300px]",
          pathEnd {
            div(cls := "text-2xl", "Index page.")
          },
          /* <focus> */
          pathPrefix("tabs") {
            MyComponent()
          },
          /* </focus> */
          (noneMatched & extractUnmatchedPath) { unmatched =>
            div(
              div(cls := "text-2xl", "Not Found"),
              div(unmatched.mkString("/", "/", ""))
            )
          }
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
              href := "/tabs/tab-1",
              "➜ /tabs/tab-1"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/tabs/tab-2",
              "➜ /tabs/tab-2"
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
