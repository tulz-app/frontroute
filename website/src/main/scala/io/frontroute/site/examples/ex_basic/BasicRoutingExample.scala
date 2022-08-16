package io.frontroute.site
package examples
package ex_basic

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString

object BasicRoutingExample
    extends CodeExample(
      id = "basic-routing",
      title = "Basic routing",
      description = FileAsString("description.md"),
      links = Seq(
        "/",
        "/new-path",
        "/legacy-path",
        "/some-section/some-page",
        "/some-section/another-page"
      )
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
            /* <focus> */
          },
          /* </focus> */
          /* <focus> */
          (path("new-path") | path("legacy-path")) {
            /* </focus> */
            div(cls := "text-2xl", "new-path OR legacy-path")
            /* <focus> */
          },
          /* </focus> */
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
                /* <focus> */
              },
              /* </focus> */
              /* <focus> */
              path("another-page") {
                /* </focus> */
                div(cls := "text-2xl", "Another page.")
                /* <focus> */
              }
              /* </focus> */
            )
            /* <focus> */
          },
          /* </focus> */
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
            /* <focus> */
          }
          /* </focus> */
        )
      )
    })
