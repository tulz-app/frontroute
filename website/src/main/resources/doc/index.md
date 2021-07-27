# frontroute

`frontroute` is a front-end router library for single-page applications (SPA) built
with [Scala.js](http://www.scala-js.org/), with an API inspired
by [Akka HTTP](https://doc.akka.io/docs/akka-http/current/).

## Getting started

`frontroute` is available for [Scala.js](http://www.scala-js.org/) `v1.6.0`+ (published for Scala 2.13 and 3.0.1).

```scala
libraryDependencies += "io.frontroute" %%% "frontroute" % "{{frontrouteVersion}}"
```

```scala
import io.frontroute._
```

For Airstream `v0.12.x`:

```scala
libraryDependencies += "io.frontroute" %%% "frontroute" % "0.12.2"
```

For Airstream `v0.11.x`:

```scala
libraryDependencies += "io.frontroute" %%% "frontroute" % "0.11.7"
```


## Sneak peek

Defining routes with `frontroute` looks like this:

```scala
import io.frontroute._
val $currentUser: Signal[User] = ???

signal($currentUser) { implicit currentUser =>
  concat(
    pathEnd {
      render(IndexPage.render)
    },
    path("sign-in") {
      render(SignInPage.render)
    },
    path("sign-up") {
      render(SignUpPage.render)
    },
    pathPrefix("account") {
      concat(
        path("settings") {
          render(AccountSettingsPage.render)
        },
        (path("things" / segment) & maybeParam("page")) { (segment: String, page: Option[String]) =>
          render(ThingsPage.render(page))
        }
      )
    }
  )
}

```

Next, see [overview](/overview) – for the overview of how `frontroute` works.

Also, check out the [reference](/reference).

