package io.frontroute.site.examples.ex_recursive_path_matching

import com.yurique.embedded.FileAsString
import io.frontroute.LocationProvider
import io.frontroute.site.examples.CodeExample
import io.laminext.AmAny
import io.laminext.AmendedHtmlTag
import org.scalajs.dom

object RecursivePathMatchingExample
    extends CodeExample(
      id = "recursive-path-matching",
      title = "Recursive path matching",
      description = FileAsString("description.md")
    )((locationProvider: LocationProvider, a: AmendedHtmlTag[dom.html.Anchor, AmAny]) => {
      import com.raquo.laminar.api.L.{a => _, _}
      import io.frontroute._

      /* <focus> */
      def recursivePathMatch: Directive[List[String]] =
        pathEnd.mapTo(List.empty[String]) | pathPrefix(segment).flatMap { head =>
          recursivePathMatch.map { rest =>
            head :: rest
          }
        }

      val (renders, route) = makeRoute[HtmlElement] { render =>
        concat(
          pathEnd {
            render { div(cls := "text-2xl", "Index page.") }
          },
          (pathPrefix("recursive") & recursivePathMatch) { segments =>
            render { div(div(cls := "text-2xl", "Recursive page."), div(s"Segments: ${segments.mkString(", ")}")) }
          },
          extractUnmatchedPath { unmatched =>
            render {
              div(
                div(cls := "text-2xl", "Not Found"),
                div(unmatched.mkString("/", "/", ""))
              )
            }
          }
        )
      }
      /* </focus> */

      div(
        div(
          cls := "p-4 min-h-[300px]",
          child <-- renders.map(_.getOrElse(div("loading...")))
        ),
        div(
          cls := "bg-blue-900 -mx-4 -mb-4 p-2",
          div(
            cls := "font-semibold text-2xl text-blue-200",
            "Navigation"
          ),
          div(
            cls := "flex flex-col",
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
        ),
        onMountCallback { ctx =>
          val _ = runRoute(route, locationProvider)(ctx.owner)
        }
      )
    })
