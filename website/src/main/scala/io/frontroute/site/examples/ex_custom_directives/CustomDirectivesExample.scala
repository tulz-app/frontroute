package io.frontroute.site
package examples
package ex_custom_directives

import com.yurique.embedded.FileAsString
import io.frontroute.site.examples.CodeExample
import io.laminext.AmAny
import io.laminext.AmendedHtmlTag
import org.scalajs.dom

object CustomDirectivesExample
    extends CodeExample(
      id = "custom-directives",
      title = "Custom directives",
      description = FileAsString("description.md")
    )((a: AmendedHtmlTag[dom.html.Anchor, AmAny]) => {
      import com.raquo.laminar.api.L.{a => _, _}
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

      val route =
        concat(
          pathEnd {
            complete { div(cls := "text-2xl", "Index page.") }
          },
          (path("movie") & longParam("id")) { movieId =>
            complete { div(div(cls := "text-2xl", "Movie page."), div(s"Movie ID (long): $movieId")) }
          },
          extractUnmatchedPath { unmatched =>
            complete {
              div(
                div(cls := "text-2xl", "Not Found"),
                div(unmatched.mkString("/", "/", ""))
              )
            }
          }
        )

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
