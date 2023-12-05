package frontroute.site.examples.ex_navmod

import frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString

object NavModExample
    extends CodeExample(
      id = "navmod",
      title = "navMod",
      description = FileAsString("description.md"),
      links = Seq(
        "/",
        "/pages/page-1",
        "/pages/page-2",
        "/pages/page-3",
        "/pages/some-page"
      )
    )(() => {
      import frontroute._
      import io.laminext.syntax.core._

      import com.raquo.laminar.api.L._

      val links = Seq(
        "page-1" -> "Page 1",
        "page-2" -> "Page 2",
        "page-3" -> "Page 3",
      )

      routes(
        div(
          cls := "p-4 min-h-[300px]",
          pathEnd {
            div(
              cls := "text-2xl",
              div(
                "Index page."
              ),
            )
          },
          pathPrefix("pages") {
            div(
              div(
                cls := "flex space-x-4",
                links.map { case (path, pageTitle) =>
                  a(
                    relativeHref(path),
                    cls := "text-xl px-4 py-1 rounded border-b-2",
                    /* <focus> */
                    navMod { active =>
                      Seq(
                        cls("border-blue-800 bg-blue-200 text-blue-800") <-- active,
                        cls("border-transparent text-blue-700") <-- !active,
                      )
                    },
                    /* </focus> */
                    pageTitle
                  )
                }
              ),
              path("page-1") {
                div(
                  cls := "text-2xl",
                  div(
                    "Page 1."
                  ),
                )
              },
              path("page-2") {
                div(
                  cls := "text-2xl",
                  div(
                    "Page 2."
                  ),
                )
              },
              path("page-3") {
                div(
                  cls := "text-2xl",
                  div(
                    "Page 3."
                  ),
                )
              },
              noneMatched {
                div(
                  cls := "text-2xl",
                  div(
                    "Unknown page."
                  ),
                )
              },
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
