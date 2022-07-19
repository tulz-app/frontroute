package io.frontroute.site
package examples
package ex_memoize

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString
import io.laminext.AmAny
import io.laminext.AmendedHtmlTag
import org.scalajs.dom

import scala.scalajs.js

object MemoizeExample
    extends CodeExample(
      id = "memoize",
      title = "Memoize",
      description = FileAsString("description.md")
    )((a: AmendedHtmlTag[dom.html.Anchor, AmAny]) => {
      import io.frontroute._
      import io.laminext.fetch._
      import com.raquo.laminar.api.L.{a => _, _}

      /* <focus> */
      def fetchData(something: String): EventStream[String] =
        Fetch
          .get(url("https://httpbin.org/get").withParams("something" -> s"give it back: $something"))
          .json
          .map { response =>
            if (response.ok) {
              response.data.asInstanceOf[js.Dynamic].args.something.asInstanceOf[String]
            } else {
              s"non-okay response: ${response.status}"
            }

          }
      /* </focus> */

      val route =
        concat(
          pathEnd {
            complete { div(cls := "text-2xl", "Index page.") }
          },
          path("memoize" / segment) { value =>
            /* <focus> */
            memoize(() => fetchData(value)) { fetched =>
              /* </focus> */
              complete {
                div(
                  div(cls := "text-2xl", "Index page."),
                  div(s"Fetched value: $fetched")
                )
              }
            }
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
          cls := "bg-blue-900 -mx-4 -mb-4 p-2 space-y-2",
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
              href := "/memoize/dog",
              "➜ /memoize/dog"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/memoize/cat",
              "➜ /memoize/cat"
            ),
            a(
              cls  := "text-blue-300 hover:text-blue-100",
              href := "/memoize/lizard",
              "➜ /memoize/lizard"
            )
          )
        )
      )
    })
