package frontroute.site.examples.ex_advanced_params

import com.yurique.embedded.FileAsString
import frontroute.site.examples.CodeExample

object AdvancedParamsExample
    extends CodeExample(
      id = "advanced-query-parameters",
      title = "Advanced query parameters",
      description = FileAsString("description.md"),
      links = Seq(
        "/",
        "/movies",
        "/movies?page=1",
        "/movies?page=2",
        "/movies?page=3",
      )
    )(() => {
      import com.raquo.laminar.api.L._
      import frontroute._
      import scala.util.Try

      /* <focus> */
      def intParam(name: String): Directive[Int] =
        param(name).emap { string =>
          Try(string.toInt).toEither
        }
      /* </focus> */

      routes(
        div(
          cls := "p-4 min-h-[300px]",
          pathEnd {
            div(cls := "text-2xl", "Index page.")
          },
          /* <focus> */
          (path("movies") & intParam("page").opt.default(0)) { movieId =>
            /* </focus> */
            div(div(cls := "text-2xl", "Movie page."), div(s"Movies, page: $movieId"))
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
