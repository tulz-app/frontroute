![Maven Central](https://img.shields.io/maven-central/v/app.tulz/laminar-router_sjs1_2.13.svg)

Routing library for [Laminar](https://github.com/raquo/Laminar) with a DSL inspired by Akka HTTP.

```scala
"io.frontroute" %%% "frontroute" % "0.11.0"   // Depends on Airstream 0.11.1 
```

![Documentation coming soon](https://img.shields.io/static/v1?label=Documentation&message=coming%20soon&color=orange)

An example is available: https://github.com/yurique/laminar-router-example

A sneak peek at the DSL (see the above example):

```scala
import com.raquo.laminar.api.L._
import app.tulz.routing._
import app.tulz.routing.directives._
import io.frontroute.{ BrowserNavigation, BrowserRouteLocationProvider }

object App {

  private val currentRender = Var[Element](
    div("initializing...")
  )

  def start(): Unit = {
    val route =
      concat(
        (pathEnd | path("index")) {
          completeRender {
            IndexPage()
          }
        },
        pathPrefix("pages") {
          concat(
            path("page-1") {
                completeRender {
                  Page1()
                }
            },
            path("page-2") {
              completeRender {
                Page2()
              }
            }
          )
        },
        completeRender {
          PageNotFound()
        }
      )
    
    runRoute(route, new BrowserRouteLocationProvider(windowEvents.onPopState)).foreach(_())(unsafeWindowOwner)
  }

  private def completeRender(r: => Element): Route =
    complete {
      currentRender.writer.onNext(r)
      org.scalajs.dom.window.scrollTo(0, 0)
    }


}

object Link {

  def apply(
    where: String,
    mods: Modifier[HtmlElement]*
  ): HtmlElement = {
    a(
      href := where,
      onClick.preventDefault --> { _ =>
        BrowserNavigation.pushState(null, null, where)
      },
      mods
    )
  }

}

object PageChrome {

  private def navLink(where: String, mods: Mod[HtmlElement]*) =
    Link(where,mods)

  def apply($child: Signal[Element]): HtmlElement =
    div(
      div(
        navLink("/index", "Index Page"),
        navLink("/pages/page-1", "Page 1"),
        navLink("/pages/page-2", "Page 2")
       ),
      div(
        child <-- $child
      )
    )

}


object IndexPage {

  def apply(): HtmlElement =
    div(
      "I'm the index page"
    )

}

object Page1 {

  def apply(): HtmlElement =
    div(
      "I'm Page 1"
    )

}

object Page2 {

  def apply(): HtmlElement =
    div(
      "I'm Page 2"
    )

}

object PageNotFound {

  def apply(): HtmlElement =
    div(
      "Not Found"
    )

}
```


