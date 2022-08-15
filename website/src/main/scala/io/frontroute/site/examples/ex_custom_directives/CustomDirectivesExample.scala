package io.frontroute.site
package examples
package ex_custom_directives

import com.yurique.embedded.FileAsString
import io.frontroute.site.examples.CodeExample

object CustomDirectivesExample
    extends CodeExample(
      id = "custom-directives",
      title = "Custom directives",
      description = FileAsString("description.md")
    )(() => {
      import com.raquo.laminar.api.L._
      import io.frontroute._
      import scala.util._

      /* <focus> */
      def longParam(paramName: String): Directive[Long] =
        param(paramName).flatMap { paramValue =>
          Try(paramValue.toLong).fold(
            _ => reject,
            parsed => provide(parsed)
          )
        }
      /* </focus> */

      div(
        div(
          cls := "p-4 min-h-[300px]",
          pathEnd {
            div(cls := "text-2xl", "Index page.")
          },
          (path("movie") &
            /* <focus> */
            longParam("id") /* </focus> */
          ) { movieId =>
            div(div(cls := "text-2xl", "Movie page."), div(s"Movie ID (long): $movieId"))
          },
          (noneMatched & extractUnmatchedPath) { unmatched =>
            div(
              div(cls := "text-2xl", "Not Found"),
              div(unmatched.mkString("/", "/", ""))
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
              href := "/movie?id=1",
              "➜ /movie?id=1"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/movie?id=2",
              "➜ /movie?id=2"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/movie?id=not-long",
              "➜ /movie?id=not-long"
            )
          )
        )
      )
    })
