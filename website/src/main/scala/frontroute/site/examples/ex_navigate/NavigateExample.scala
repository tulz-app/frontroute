package frontroute.site.examples.ex_navigate

import frontroute.site.examples.CodeExample
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
        "/special-deal",
        "/some-page"
      )
    )(() => {
      import frontroute._
      import io.laminext.syntax.core._

      import com.raquo.laminar.api.L._

      routes(
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
                (maybeParam("deal-id") & multiParam("some-id")) { (dealId, someIds) =>
                  div(
                    div(
                      "car details ..."
                    ),
                    div(
                      s"Deal ID: ${dealId}"
                    ),
                    div(
                      s"Some IDs: ${someIds}"
                    )
                  )
                }
              }
            )
          },
          path("special-deal") {
            /* <focus> */
            navigate(
              "/cars/2/details",
              Map(
                "deal-id" -> Seq("annual-new-year"),
                "some-id" -> Seq("456", "997"),
              )
            )
            /* </focus> */
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
