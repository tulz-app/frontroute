# frontroute

`frontroute` is a router library for [Scala.js](https://www.scala-js.org/) + [Laminar](https://laminar.dev/) applications.

...with an API (DSL) inspired by [Akka HTTP](https://doc.akka.io/docs/akka-http/current/):

```scala
import com.raquo.laminar.api.L.*
import io.frontroute.*

div(
  pathEnd {
    IndexPage()
  },
  path("sign-in") {
    SignInPage()
  },
  path("sign-up") {
    SignUpPage()
  },
  noneMatched {
    NotFoundPage()
  }
)
```

More examples [here]({{sitePrefix}}/examples).

