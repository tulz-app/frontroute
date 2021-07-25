package io.frontroute.site.examples.ex_path_matching

import com.yurique.embedded.FileAsString
import io.frontroute.LocationProvider
import io.frontroute.site.examples.CodeExample
import io.laminext.AmAny
import io.laminext.AmendedHtmlTag
import org.scalajs.dom

object PathMatchingExample
    extends CodeExample(
      id = "path-matching",
      title = "Path matching",
      description = FileAsString("description.md")
    )((locationProvider: LocationProvider, a: AmendedHtmlTag[dom.html.Anchor, AmAny]) => {
      import com.raquo.laminar.api.L.{a => _, _}
      import io.frontroute._

      /* <focus> */
      val (renders, route) = makeRoute[HtmlElement] { render =>
        concat(
          pathEnd {
            render { div(cls := "text-2xl", "Index page.") }
          },
          path("user" / long) { userId =>
            render { div(div(cls := "text-2xl", "User page."), div(s"User ID: $userId")) }
          },
          path(Set("page-1", "page-2", "page-3")) { page =>
            render { div(div(cls := "text-2xl", "Some page."), div(s"Page name: $page")) }

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
              cls := "text-blue-300 hover:text-blue-100",
              href := "/",
              "➜ /"
            ),
            a(
              cls := "text-blue-300 hover:text-blue-100",
              href := "/user/1",
              "➜ /user/1"
            ),
            a(
              cls := "text-blue-300 hover:text-blue-100",
              href := "/user/2",
              "➜ /user/2"
            ),
            a(
              cls := "text-blue-300 hover:text-blue-100",
              href := "/user/not-long",
              "➜ /user/not-long"
            ),
            a(
              cls := "text-blue-300 hover:text-blue-100",
              href := "/page-1",
              "➜ /page-1"
            ),
            a(
              cls := "text-blue-300 hover:text-blue-100",
              href := "/page-2",
              "➜ /page-2"
            ),
            a(
              cls := "text-blue-300 hover:text-blue-100",
              href := "/page-3",
              "➜ /page-3"
            ),
            a(
              cls := "text-blue-300 hover:text-blue-100",
              href := "/page-4",
              "➜ /page-4"
            )
          )
        ),
        onMountCallback { ctx =>
          val _ = runRoute(route, locationProvider)(ctx.owner)
        }
      )
    })
