package frontroute.site.examples.ex_multiparams

import com.yurique.embedded.FileAsString
import frontroute.site.examples.CodeExample

object MultiParamsExample
    extends CodeExample(
      id = "multi-parameters",
      title = "Query multi-parameters",
      description = FileAsString("description.md"),
      links = Seq(
        "/",
        "/movies/search?director=cameron&director=spielberg&year=1991&year=1992&year=1993",
        "/movies/search?director=cameron&director=quentin",
        "/movies/search?year=1991&year=1992&year=1994&year=1995"
      )
    )(() => {
      import com.raquo.laminar.api.L._
      import frontroute._

      routes(
        div(
          cls := "p-4 min-h-[300px]",
          pathEnd {
            div(cls := "text-2xl", "Index page.")
          },
          /* <focus> */
          (
            path("movies" / "search") &
              multiParam("director") &
              multiParam("year")
          ) { (maybeDirector, maybeYear) =>
            /* </focus> */
            div(div(cls := "text-2xl", "Movie search page."), div(s"Director: $maybeDirector"), div(s"Year: $maybeYear"))
            /* <focus> */
          },
          /* </focus> */
          (noneMatched & extractUnmatchedPath) { unmatched =>
            div(
              div(cls := "text-2xl", "Not Found"),
              div(unmatched.mkString("/", "/", ""))
            )
          }
        )
      )
    })
