package io.frontroute.site
package examples
package ex_basic

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString
import io.laminext.AmAny
import io.laminext.AmendedHtmlTag
import org.scalajs.dom

object BasicRoutingExample
    extends CodeExample(
      id = "basic-routing",
      title = "Basic routing",
      description = FileAsString("description.md")
    )((a: AmendedHtmlTag[dom.html.Anchor, AmAny]) => {
      import com.raquo.laminar.api.L.{a => _, _}
      /* <focus> */
      import io.frontroute._
      /* </focus> */

      /* <focus> */
      val route =
        concat(
          pathEnd {
            div(cls := "text-2xl", "Index page.")
          },
          (path("new-path") | path("legacy-path")) {
            div(cls := "text-2xl", "new-path OR legacy-path")
          },
          pathPrefix("some-section") {
            concat(
              path("some-page") {
                div(cls := "text-2xl", "Some page.")
              },
              path("another-page") {
                div(cls := "text-2xl", "Another page.")
              }
            )
          },
          extractUnmatchedPath { unmatched =>
            div(
              div(cls := "text-2xl", "Not Found"),
              div(unmatched.mkString("/", "/", ""))
            )
          }
        )
      /* </focus> */

      div(
        div(
          cls := "p-4 min-h-[300px]",
          route
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
