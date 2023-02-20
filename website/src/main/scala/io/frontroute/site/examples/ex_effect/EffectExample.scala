package io.frontroute.site.examples.ex_effect

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString

object EffectExample
    extends CodeExample(
      id = "run-effect",
      title = "Run Effect",
      description = FileAsString("description.md"),
      links = Seq(
        "/",
        "/effect-1",
        "/effect-2",
        "/some-page"
      )
    )(() => {
      import io.frontroute._

      import io.laminext.syntax.core._
      import com.raquo.laminar.api.L._
      import org.scalajs.dom

      routes(
        div(
          cls := "p-4 min-h-[300px]",
          /* <focus> */
          pathEnd {
            /* </focus> */
            div(cls := "text-2xl", "Index page.")
            /* <focus> */
          },
          path("effect-1") {
            runEffect {
              dom.console.log("effect 1")
            }
          },
          path("effect-2") {
            runEffect {
              dom.console.log("effect 2")
            }
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
