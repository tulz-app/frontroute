```scala
import io.frontroute._
import com.raquo.laminar.api.L._

val (renders, route) = makeRoute[HtmlElement] { render =>
  concat(
    pathEnd {
      render { div("path is /") }
    },
    (path("new-path") | path("legacy-path")) {
      render { div("path is /new-path OR /legacy-path") }      
    },
    pathPrefix("some-section") {
      concat(
        path("some-page") {
          render { div("path is /some-section/some-page") }
        },
        (path("another-page") & param("some-param")) { paramValue =>
          render { div(s"path is /some-section/another-page param is: ${paramValue}") }
        }
      )
    }
  )
}

val locationProvider = LocationProvider.browser(windowEvents.onPopState)

val appContainer = dom.document.querySelector("#app")

render(
  appContainer,
  div(
    child <-- renders.map(_.getOrElse(div("loading...")))
  )
)

runRoute(route, locationProvider)(unsafeWindowOwner)
```
