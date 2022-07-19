package io.frontroute.site
package examples
package ex_params

import com.yurique.embedded.FileAsString
import io.frontroute.site.examples.CodeExample
import io.laminext.AmAny
import io.laminext.AmendedHtmlTag
import org.scalajs.dom

object ParamsExample
    extends CodeExample(
      id = "query-parameters",
      title = "Query parameters",
      description = FileAsString("description.md")
    )((a: AmendedHtmlTag[dom.html.Anchor, AmAny]) => {
      import com.raquo.laminar.api.L.{a => _, _}
      import io.frontroute._

      val route =
        concat(
          pathEnd {
            div(cls := "text-2xl", "Index page.")
          },
          /* <focus> */
          (path("movie") & param("id")) { movieId =>
            /* </focus> */
            div(div(cls := "text-2xl", "Movie page."), div(s"Movie ID: $movieId"))
          },
          /* <focus> */
          (path("movies" / "search") & maybeParam("director") & maybeParam("year")) { (maybeDirector, maybeYear) =>
            /* </focus> */
            div(div(cls := "text-2xl", "Movie search page."), div(s"Director: $maybeDirector"), div(s"Year: $maybeYear"))
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
              href := "/movies/search?director=nolan&year=2014",
              "➜ /movies/search?director=nolan&year=2014"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/movies/search?director=nolan",
              "➜ /movies/search?director=nolan"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/movies/search?year=2014",
              "➜ /movies/search?year=2014"
            )
          )
        )
      )
    })
