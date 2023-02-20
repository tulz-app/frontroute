package io.frontroute.site.examples.ex_execute

import io.frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString

object ExecuteExample
    extends CodeExample(
      id = "execute",
      title = "Execute",
      description = FileAsString("description.md"),
      links = Seq(
        "/",
        "/execute-1",
        "/execute-2",
        "/execute-2/sub-1",
        "/execute-2/sub-2",
        "/execute-2/sub-2/sub-sub-1",
        "/execute-2/sub-2/sub-sub-2",
        "/some-page"
      )
    )(() => {
      import io.frontroute._
      import com.raquo.laminar.api.L._
      import org.scalajs.dom

      routes(
        div(
          cls := "p-4 min-h-[300px]",
          pathEnd.execute {
            dom.console.log("root")
          },
          path("execute-1").execute {
            dom.console.log("execute-1")
          },
          pathPrefix("execute-2") {
            firstMatch(
              pathEnd.execute {
                dom.console.log("execute-2")
              },
              path(segment).execute { s =>
                dom.console.log(s"execute-2/${s}")
              },
              path(segment / segment).execute { case (s1, s2) =>
                dom.console.log(s"execute-2/${s1}/${s2}")
              },
            )
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
