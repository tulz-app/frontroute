package io.frontroute.site
package examples
package ex_path_matching

import com.yurique.embedded.FileAsString
import io.frontroute.site.examples.CodeExample

object PathMatchingExample
    extends CodeExample(
      id = "path-matching",
      title = "Path matching",
      description = FileAsString("description.md")
    )(() => {
      import com.raquo.laminar.api.L._
      import io.frontroute._

      div(
        div(
          cls := "p-4 min-h-[300px]",
          /* <focus> */
          pathEnd {
            /* </focus> */
            div(cls := "text-2xl", "Index page.")
            /* <focus> */
          },
          /* </focus> */
          /* <focus> */
          path("user" / long) { userId =>
            /* </focus> */
            div(div(cls := "text-2xl", "User page."), div(s"User ID: $userId"))
            /* <focus> */
          },
          /* </focus> */
          /* <focus> */
          path(Set("page-1", "page-2", "page-3")) { page =>
            /* </focus> */
            div(div(cls := "text-2xl", "Some page."), div(s"Page name: $page"))
            /* <focus> */
          },
          /* </focus> */
          /* <focus> */
          (noneMatched & extractUnmatchedPath) { unmatched =>
            /* </focus> */
            div(
              div(cls := "text-2xl", "Not Found"),
              div(unmatched.mkString("/", "/", ""))
            )
            /* <focus> */
          }
          /* </focus> */
        ),
        div(
          cls := "bg-blue-900 -mx-4 -mb-4 p-2",
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
              href := "/user/1",
              "➜ /user/1"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/user/2",
              "➜ /user/2"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/user/not-long",
              "➜ /user/not-long"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/page-1",
              "➜ /page-1"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/page-2",
              "➜ /page-2"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/page-3",
              "➜ /page-3"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/page-4",
              "➜ /page-4"
            )
          )
        )
      )
    })
