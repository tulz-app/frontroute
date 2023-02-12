package io.frontroute.site.examples.ex_navigate

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString

object NavigateExample
    extends CodeExample(
      id = "navigate",
      title = "Navigate",
      description = FileAsString("description.md"),
      links = Seq(
        "/",
        "/cars/1",
        "/cars/1/legacy-summary",
        "/cars/1/summary",
        "/cars/1/details",
        "/cars/2/legacy-summary",
        "/cars/2/summary",
        "/cars/2/details",
        "/some-page"
      )
    )(() => {
      import io.frontroute._
      import io.laminext.syntax.core._

      import com.raquo.laminar.api.L._

      div(
        cls := "p-4 min-h-[300px]",
        pathEnd {
          div(
            cls := "text-2xl",
            div(
              "Index page."
            ),
          )
        },
        pathPrefix("cars" / segment) { carId =>
          div(
            div(
              cls := "text-2xl",
              s"Car $carId"
            ),
            /* <focus> */
            (pathEnd | testPath("legacy-summary")) {
              navigate("summary", replace = true)
            },
            /* </focus> */
            path("summary") {
              div("car summary ...")
            },
            path("details") {
              div("car details ...")
            }
          )
        },
        (noneMatched & extractUnmatchedPath) { unmatched =>
          div(
            div(cls := "text-2xl", "Not Found"),
            div(unmatched.mkString("/", "/", ""))
          )
        }
      )
    })
