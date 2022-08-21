`frontroute` is a router library for [Scala.js](https://www.scala-js.org/) + [Laminar](https://laminar.dev/) applications.

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

See [Getting started](/examples) for installation instructions.