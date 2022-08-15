package io.frontroute.site
package examples
package ex_recursive_path_matching

import com.yurique.embedded.FileAsString
import io.frontroute.site.examples.CodeExample

object RecursivePathMatchingExample
    extends CodeExample(
      id = "recursive-path-matching",
      title = "Recursive path matching",
      description = FileAsString("description.md")
    )(() => {
      import com.raquo.laminar.api.L._
      import io.frontroute._

      /* <focus> */
      def recursivePathMatch: Directive[List[String]] =
        pathEnd.mapTo(List.empty[String]) | pathPrefix(segment).flatMap { head =>
          recursivePathMatch.map { rest =>
            head :: rest
          }
        }
      /* </focus> */

      val route =
        concat(
          pathEnd {
            div(cls := "text-2xl", "Index page.")
          },
          (pathPrefix("recursive") &
            /* <focus> */
            recursivePathMatch
          /* </focus> */
          ) { segments =>
            div(div(cls := "text-2xl", "Recursive page."), div(s"Segments: ${segments.mkString(", ")}"))
          },
          extractUnmatchedPath { unmatched =>
            div(
              div(cls := "text-2xl", "Not Found"),
              div(unmatched.mkString("/", "/", ""))
            )
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
              href := "/recursive",
              "➜ /recursive"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/recursive/1",
              "➜ /recursive/1"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/recursive/1/2",
              "➜ /recursive/1/2"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/recursive/1/2/3",
              "➜ /recursive/1/2/3"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/recursive/1/2/3/4",
              "➜ /recursive/1/2/3/4"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/recursive/1/2/3/4/5",
              "➜ /recursive/1/2/3/4/5"
            )
          )
        )
      )
    })
