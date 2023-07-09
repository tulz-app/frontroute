`frontroute` is a router library for [Scala.js](https://www.scala-js.org/) + [Laminar](https://laminar.dev/) applications.

```scala
import com.raquo.laminar.api.L.*
import frontroute.*

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

See [getting started](/getting-started).

## Installation

### Prerequisites

* [Scala.js](https://www.scala-js.org/) `v{{scalajsVersion}}`+
* Scala `2.13` or `{{scala3version}}`+
* [Laminar](https://laminar.dev/) `{{laminarVersion}}`

### sbt

Add the [Scala.js](https://www.scala-js.org/) plugin to your `project/plugins.sbt` file.

```scala
addSbtPlugin("org.scala-js" % "sbt-scalajs"  % {{scalajsVersion}})
```

Enable the plugin and add the `frontroute` library to your `build.sbt` file:

```scala
enablePlugins(ScalaJSPlugin)

libraryDependencies ++= Seq(
  "io.frontroute" %%% "frontroute" % "{{frontrouteVersion}}"
)
```

### Mill

```scala
import $ivy.`com.lihaoyi::mill-contrib-bloop:$MILL_VERSION`
import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalajslib.api._

object counter extends ScalaJSModule {
    def scalaVersion   = "{{scala3version}}"
    def scalaJSVersion = "{{scalajsVersion}}"
    
    def ivyDeps = Agg(ivy"io.frontroute::frontroute::{{frontrouteVersion}}")
    
    override def moduleKind = T(mill.scalajslib.api.ModuleKind.CommonJSModule)
}
```

### Previous versions

* [frontroute `v0.17.x`](https://frontroute.dev/v/0.17.x/) (Laminar 15.x)
* [frontroute `v0.16.x`](https://frontroute.dev/v/0.16.x/)
* [frontroute `v0.15.x`](https://frontroute.dev/v/0.15.x/)

---

Older versions of `frontroute` are no longer maintained and documentation is not available.


