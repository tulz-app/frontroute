package frontroute.site
package examples
package ex_recursive_path_matching

import com.yurique.embedded.FileAsString
import frontroute.site.examples.CodeExample

object RecursivePathMatchingExample
    extends CodeExample(
      id = "recursive-path-matching",
      title = "Recursive path matching",
      description = FileAsString("description.md"),
      links = Seq(
        "/",
        "/recursive",
        "/recursive/1",
        "/recursive/1/2",
        "/recursive/1/2/3",
        "/recursive/1/2/3/4",
        "/recursive/1/2/3/4/5"
      )
    )(() => {
      import com.raquo.laminar.api.L._
      import frontroute._

      def recursivePathMatch: Directive[List[String]] =
        /* <focus> */
        pathEnd.mapTo(List.empty[String]) | pathPrefix(segment).flatMap { head =>
          recursivePathMatch.map { rest =>
            head :: rest
          }
        }
      /* </focus> */

      routes(
        div(
          cls := "p-4 min-h-[300px]",
          pathEnd {
            div(cls := "text-2xl", "Index page.")
          },
          (
            pathPrefix("recursive") &
              /* <focus> */
              recursivePathMatch
              /* </focus> */
          ) { segments =>
            div(div(cls := "text-2xl", "Recursive page."), div(s"Segments: ${segments.mkString(", ")}"))
          },
          (noneMatched & extractUnmatchedPath) { unmatched =>
            div(
              div(cls := "text-2xl", "Not Found"),
              div(unmatched.mkString("/", "/", ""))
            )
          }
        )
      )
    })
