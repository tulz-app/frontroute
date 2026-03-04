package frontroute.site.examples.ex_matched_path

import frontroute.site.examples.CodeExample
import com.yurique.embedded.FileAsString
import scala.scalajs.js.timers

object MatchedPathExample
    extends CodeExample(
      id = "matched-path",
      title = "Matched Path",
      description = FileAsString("description.md"),
      links = Seq(
        "/example-basename/",
        "/example-basename",
        "/example-basename/tabs/tab-1",
        "/example-basename/tabs/tab-2",
        "/example-basename/tabs/tab-3",
        "/example-basename/some-page"
      )
    )(() => {
      import frontroute.*
      import io.laminext.syntax.core.*

      import com.raquo.laminar.api.L.*

      def ShowCurrentPath(label: String): Element =
        div(
          div(
            cls := "bg-yellow-200 text-yellow-900 rounded-sm space-x-2 text-sm px-2 font-mono",
            div(label),
            div(
              /* <focus> */
              withMatchedPath { (baseName, path) =>
                div(
                  div(
                    cls := "flex items-center gap-2",
                    span("baseName:"),
                    span(
                      s"'${baseName}'"
                    )
                  ),
                  div(
                    span("path:"),
                    span(
                      child.text <-- path.map(s => s"'${s.mkString("/", "/", "")}'")
                    )
                  )
                )
              }
              /* </focus> */
            )
          )
        )

      val tabs = Seq(
        "tab-1" -> "Tab 1",
        "tab-2" -> "Tab 2",
      )

      def MyComponent(): Element =
        div(
          cls := "space-y-2",
          div(
            cls := "flex space-x-2",
            tabs.map { case (path, tabLabel) =>
              a(
                /* <focus> */
                relativeHref(path),
                /* </focus> */
                cls := "text-xl px-4 py-1 rounded border-b-2",
                /* <focus> */
                navMod { active =>
                  Seq(
                    cls("border-blue-800 bg-blue-200 text-blue-800") <-- active,
                    cls("border-transparent text-blue-700") <-- !active,
                  )
                },
                /* </focus> */
                tabLabel,
              )
            },
            a(
              href := "tab-3",
              cls  := "text-xl px-4 py-1 rounded border-b-2",
              navMod { active =>
                Seq(
                  cls("border-blue-800 bg-blue-200 text-blue-800") <-- active,
                  cls("border-transparent text-blue-700") <-- !active,
                )
              },
              "Tab 3",
            )
          ),
          div(
            ShowCurrentPath("Inside component:"),
            path("tab-1") {
              div(
                cls := "bg-blue-100 text-blue-600 p-4",
                div("Content one."),
                div(a(href := "/tabs/tab-2", "Show Tab 2")),
                ShowCurrentPath("Inside tab-1:"),
              )
            },
            path("tab-2") {
              div(
                cls := "bg-blue-100 text-blue-600 p-4",
                div("Content two"),
                ShowCurrentPath("Inside tab-2:"),
              )
            },
            path("tab-3") {
              div(
                cls := "bg-blue-100 text-blue-600 p-4",
                div("Content three"),
                ShowCurrentPath("Inside tab-3:"),
              )
            },
          )
        )

      /* <focus> */
      routes(baseName = "/example-basename")(
        /* </focus> */
        div(
          cls := "p-4 min-h-[300px]",
          pathEnd {
            div(
              cls := "text-2xl",
              div(
                "Index page."
              ),
              ShowCurrentPath("Inside index:")
            )
          },
          pathPrefix("tabs") {
            div(
              MyComponent()
            )
          },
          (noneMatched & extractUnmatchedPath) { unmatched =>
            div(
              div(cls := "text-2xl", "Not Found"),
              div(unmatched.mkString("/", "/", ""))
            )
          }
        ),
        div(
          cls := "p-4",
          div("Relative links, '/example-basename' will be auto-prepended):"),
          ul(
            Seq(
              "/",
              "/tabs/tab-1",
              "/tabs/tab-2",
              "/tabs/tab-3",
              "/some-page"
            ).map { path =>
              li(
                a(
                  cls  := "text-blue-700 hover:text-blue-600",
                  /* <focus> */
                  href := path,
                  /* </focus> */
                  s"➜ $path"
                )
              )
            },
            li(
              a(
                cls := "text-blue-700 hover:text-blue-600",
                /* <focus> */
                // This href is updated dynamically, the baseName will be updated after every update.
                href <-- EventStream.periodic(5000).toSignal(2).map { i => s"/tabs/tab-${i % 3 + 1}" },
                /* </focus> */
                s"➜ changing tab"
              )
            ),
            li(
              a(
                cls := "text-blue-700 hover:text-blue-600",
                onMountUnmountCallbackWithState(
                  ctx => {
                    var i = 0
                    ctx.thisNode.ref.setAttribute("href", s"/tabs/tab-1")
                    timers.setInterval(5000) {
                      i += 1
                      /* <focus> */
                      // This href is updated dynamically, twice in a row. The baseName will be updated after every update.
                      ctx.thisNode.ref.setAttribute("href", s"/tabs/tab-${i % 3 + 1}")
                      ctx.thisNode.ref.setAttribute("href", s"/tabs/tab-${(i + 1) % 3 + 1}")
                      /* </focus> */
                    }
                  },
                  (_, timer) => {
                    timers.clearInterval(timer)
                  }
                ),
                s"➜ another changing tab"
              )
            ),
            /* <focus> */
            // Here, we have an anchor that is being dynamically added and removed from the dom, and its href is also updated dynamically.
            // The baseName will be updated after every update.
            /* </focus> */
            child <-- EventStream.periodic(5000).toSignal(1).map { i =>
              if (i % 2 == 0) {
                li(
                  a(
                    cls := "text-blue-700 hover:text-blue-600",
                    href <-- EventStream.periodic(5000).toSignal(2).map { i => s"/tabs/tab-disappearing-${i % 3 + 1}" },
                    s"➜ disappearing link"
                  )
                )
              } else {
                li(span("--"))
              }
            }
          )
        )
      )
    })
