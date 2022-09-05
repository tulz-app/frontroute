package io.frontroute.site.examples.ex_nested

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString

object NestedExample
    extends CodeExample(
      id = "nested-routes",
      title = "Nested routes",
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

      def MyComponent(): Element =
        div(
          cls := "space-y-2",
          /* <focus> */
          path(segment).signal { tab =>
            /* </focus> */
            div(
              cls := "flex space-x-2",
              a(
                href := "tab-1",
                cls  := "text-xl px-4 py-1 rounded border-b-2",
                /* <focus> */
                cls.toggle("border-blue-800 bg-blue-200 text-blue-800") <-- tab.map(_ == "tab-1"),
                cls.toggle("border-transparent text-blue-700") <-- tab.map(_ != "tab-1"),
                /* </focus> */
                "Tab 1"
              ),
              a(
                href := "tab-2",
                cls  := "text-xl px-4 py-1 rounded border-b-2",
                /* <focus> */
                cls.toggle("border-blue-800 bg-blue-200 text-blue-800") <-- tab.map(_ == "tab-2"),
                cls.toggle("border-transparent text-blue-700") <-- tab.map(_ != "tab-2"),
                /* </focus> */
                "Tab 2"
              )
            )
            /* <focus> */
          },
          /* </focus> */
          div(
            /* <focus> */
            path("tab-1") {
              /* </focus> */
              div("Content one.", cls := "bg-blue-100 text-blue-600 p-4")
              /* <focus> */
            },
            /* </focus> */
            /* <focus> */
            path("tab-2") {
              /* </focus> */
              div("Content two", cls := "bg-blue-100 text-blue-600 p-4")
              /* <focus> */
            }
            /* </focus> */
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
        )
      )
    })
