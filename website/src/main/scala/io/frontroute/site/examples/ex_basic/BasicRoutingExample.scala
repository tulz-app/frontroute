package io.frontroute.site
package examples
package ex_basic

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString

object BasicRoutingExample
    extends CodeExample(
      id = "basic-routing",
      title = "Basic routing",
      description = FileAsString("description.md")
    )(() => {
      import com.raquo.laminar.api.L._
      /* <focus> */
      import io.frontroute._
      /* </focus> */

      div(
        div(
          cls := "p-4 min-h-[300px]",
          /* <focus> */
          pathEnd {
            /* </focus> */
            div(cls := "text-2xl", "Index page.")
          },
          /* <focus> */
          (path("new-path") | path("legacy-path")) {
            /* </focus> */
            div(cls := "text-2xl", "new-path OR legacy-path")
          },
          /* <focus> */
          pathPrefix("some-section") {
            /* </focus> */
            /* <focus> */
            concat(
              /* </focus> */
              /* <focus> */
              path("some-page") {
                /* </focus> */
                div(cls := "text-2xl", "Some page.")
              },
              /* <focus> */
              path("another-page") {
                /* </focus> */
                div(cls := "text-2xl", "Another page.")
              }
            )
          },
          /* <focus> */
          (noneMatched & extractUnmatchedPath) { unmatched =>
            /* </focus> */
            div(
              div(cls := "text-2xl", "Not Found"),
              div(
                cls   := "flex items-center space-x-2",
                span("Not found path:"),
                span(unmatched.mkString("/", "/", ""))
              )
            )
          }
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
              href := "/new-path",
              "➜ /new-path"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/legacy-path",
              "➜ /legacy-path"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/some-section/some-page",
              "➜ /some-section/some-page"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/some-section/another-page",
              "➜ /some-section/another-page"
            )
          )
        )
      )
    })
