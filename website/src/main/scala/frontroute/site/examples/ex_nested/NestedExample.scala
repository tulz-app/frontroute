package frontroute.site.examples.ex_nested

import frontroute.site.examples.CodeExample
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
      import frontroute._
      import io.laminext.syntax.core._
      import com.raquo.laminar.api.L._

      val tabs = Seq(
        "tab-1" -> "Tab 1",
        "tab-2" -> "Tab 2",
      )

      def MyComponent(): HtmlElement =
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
          div(
            pathEnd {
              div(
                div(cls := "text-2xl", "Index page"),
              )
            },
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

      routes(
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
