# frontroute

![Maven Central](https://img.shields.io/maven-central/v/io.frontroute/frontroute_sjs1_2.13.svg)

A routing library for [Laminar](https://github.com/raquo/Laminar).

It doesn't actually have Laminar as a dependency (though it depends on Laminar's companion - [Airstream](https://github.com/raquo/Airstream)), 
but it's intended to be used with it.

### Adding to your project

```scala
"io.frontroute" %%% "frontroute" % "0.11.1"  
```

Dependencies: [Airstream](https://github.com/raquo/Airstream) v0.11.1 and [app.tulz.tuplez](https://github.com/tulz-app/tuplez/)

```scala
import io.frontroute._
import io.frontroute.directives._
```

## Overview

`frontroute` provides a DSL inspired by Akka HTTP:

```scala

val route =
  concat(
    pathEnd {
      // do something when the path is /
    },
    path("some-page") {
      // do something when the path is /some-page
    }
  )
```

`frontroute` is rather un-opinionated about the "do something" part in the above example, but more on that - below.

Similar to Akka HTTP, the main building block used to describe the routing with `frontroute` is a directive.

### Directives 

Directives are the building blocks used to build the route (like `path` or `concat` in the above example).
Directives can be nested and combined using combinators like `map`, `flatMap`, `filter`, `collect`, `&` and `|`.

There is a set of basic directives available out of the box:

* `pathEnd`
* `path`
* `pathPrefix`
* `param`
* `maybeParam`
* `extractUnmatchedPath`
* `provide` / `tprovide`
* `signal`

One can define most of the routing for the app using those, but it is possible (and encouraged) to implement custom directives.

Directives are designed to be nested (as well as combined with `&` and `|`). 

Whenever a directive matches it gives control to the nested directive, providing the value it "extracted" (if not `Unit`):

```scala
concat( // this is not "nesting", unlike most of the following "calls"
  pathPrefix("public") { // provides no value, Unit - no need to type "_ =>"
    concat( // not "nesting"
      pathPrefix("articles") { // no value
        path(segment) { articleId => // a String value provided 
          renderArticlePage(articleId)  
        }
      },
      (pathPrefix("books") & maybeParam("author") & maybeParam("title")) { 
        (maybeAuthor, maybeTitle) => 
          // no value from the pathPrefix, combined with Option[String] value from the first param directive and Option[String] from the second one
          // the internal value is a 2-tuple - (Option[String], Option[String])
          // but here, when nesting it's not a single-parameter like Function1[Tuple2[Option[String], Option[String]], ???]
          // it's a 2-parameter function Function2[Option[String], Option[String], ???]
          renderBookSearchPage(maybeAuthor, maybeTitle)
      }
    )
  },
  pathPrefix("admin") { // provides no value
    // ... you get the idea
  }
)

```

## Usage

After you have your routes defined:

```scala 
val route =
  concat(
    pathEnd { ... },
    ...
  )
```

you need to run the `runRoute` function, passing in the route and a thing called `RouteLocationProvider`. 

`RouteLocationProvider` is a simple trait:

```scala
trait RouteLocationProvider {
  def stream: EventStream[RouteLocation]
}
```

and its single job is to provide the stream of `RouteLocation`. You can implement it depending on your needs (for tests, for example).

But, most of the times, you will probably be using the provided `BrowserRouteLocationProvider` - it takes a stream of `PopStateEvent` and 
parses the `dom.window.location` to produce the corresponding `RouteLocation`s.



```scala
val provideLocationProvider: RouteLocationProvider = new BrowserRouteLocationProvider(windowEvents.onPopState)
runRoute(route, routeLocationProvider)
```

`runRoute` also requires an implicit `Owner` (`unsafeWindowOwner` will work perfectly fine most of the times).

Under the hood,

* `runRoute` transforms a stream of `RouteLocation` (that you provide initially) into a stream 
of functions (`() => Unit`)
* it subscribes to this stream, and executes those functions as they come through,
* `runRoute` returns a `Subscription` which can be used to stop this process.

## Route actions

So far we haven't touched on how to actually do anything when a particular route is matched. 

Let's look at another small example:


```scala
concat(
  path("page-1") {
    // do something when the path is /page-1
  },
  path("page-2") {
    // do something when the path is /page-2
  }
)
```

### The "complete" directive

In a simple case, the "do something" here means "execute some code" (do `console.log`, update the "current page" signal , etc).

For that, there is a built-in directive - `complete`.

> Side note: it is not really a directive, but rather a function that terminates a 
tree of directives (by returning a `Route` which eventually gets used to build the root `Route`).
> But that is not important from a user's stand-point, 
and it might be easier to think about "complete" as of just another directive. 

It accepts a by-name block of code that will get executed whenever the route is matched:

```scala
def complete[T](action: => Unit): Route
```

The example would look like the following:

```scala
concat(
  path("page-1") {
    dom.console.log("page-1 route matched")
  },
  path("page-2") {
    dom.console.log("page-2 route matched")
  }
)
```

### The "completeN" directive

A more powerful version of `complete` is `completeN`:

```scala
def completeN[T](events: EventStream[() => Unit]): Route
```

* `completeN` accepts a stream of `() => Unit` functions

When the route is matched,
* subscribes to this stream,
* "executes" the functions emitted by the stream.

As soon as the route changes (another `complete` or `completeN` is "triggered"), the subscription is cancelled.

### How to use the "complete" directives

Let's look at an example of how you can do something useful with these "directives". 

Say, for example, you have a `Signal` for your "current page", defined with a `Var`:

```scala
sealed trait Page 
object Page { 
  case object Blank extends Page
  case object Page1 extends Page
  case object Page2 extends Page
}
```

```scala
val currentPage = Var[Page](Page.Blank)
```

You might define a custom "directive" (again, it will not be a real directive as we're going to build it using `complete`)
that updates the value of the `currentPage`:

```scala
def render(page: Page): Route =
  complete {
    currentPage.writer.onNext(page)
  }
```

Then, our example would look like this:

```scala
concat(
  path("page-1") {
    render(Page.Page1)
  },
  path("page-2") {
    render(Page.Page2)
  }
)
```

### A more complicated use case

If you wanted to get some data from the back-end before rendering a page, you could use `completeN`.
Also, this will prevent "this" action from taking effect when the call to the back-end returns after 
the route has changed and another "complete" is in effect.

For example, let's say we want to be displaying a "loading" screen while the data is being requested and after that - the actual page.

```scala
...
final case object Loading extends Page
final case class UserPage(data: UserData) extends Page
...
```

```scala
def renderF(pageFuture: => Future[Page]): Route =
  completeN {
    EventStream.merge(
      EventStream.fromValue(
        () => currentPage.writer.onNext(Page.Loading), 
        emitOnce = true
      ), 
      EventStream.fromFuture(pageFuture).map { page =>
        () => currentPage.writer.onNext(page)
      }
    )
  }
```

```scala
concat(
  // ...
  path("user" / segment) { userId =>
    renderF(
      API.getUserData(userId).map ( userData => 
        Page.UserPage(userData)
      )      
    )
  }
  // ...
)
```

As a side note, in order to keep the routes readable as they grow, it is recommended to extract 
the "actions":

```scala
val route = concat(
  // ...
  path("user" / segment) { userId => userByIdPage(userId) },
  path("user" / segment / "details") { userId => userByIdDetailsPage(userId) }
  // ...
)

private def userByIdPage(userId: String) =
  renderF {
    API.getUserData(userId).map { userData =>
      Page.UserPage(userData)
    }
  }
  
private def userByIdDetailsPage(userId: String) =
  renderF {
    API.getUserDetails(userId).map { userDetails =>
      Page.UserDetailsPage(userDetails)
    }
  }
```

## Custom directives

On the low level, in order to create a custom directive you need a `(L => Route) => Route` function. Simple, right? :)

The good thing, though, is you will most likely not need to do anything low-level - most directives are supposed to be built from the 
existing directives using the combinators.

Say, you wanted to check if part of the path is a number. 

Let's create an `isNumber` directive for that, we will use it like this:

```scala
val route = concat(
  // ...
  path("user" / segment) { userId =>
    isNumber(userId) { userIdAsInt =>
      userByIdPage(userId) 
    }
  }
  // ...
)
```

> For this particular case, defining a custom path matcher would make more sense, but let's try this anyway
> to keep things simple.


Let's define the directive now:

```scala
  def isNumber(s: String): Directive1[Int] = {
  // Directive1 means the directive provides a single value (vs. a tuple)
  // Directive0 means the directive does not provide any value at all, it just either matches or rejects
    Try(s.toInt) match {
      case Success(int) => provide(int)
      case Failure(_) => reject
    }
  
  }
```

Now, this is an extemely simple example, but most directives, no matter how complicated, can be
defined as easily from the more powerful existing directives and more powerful combinators (like, for example, the `signal` directive or the `flatMap` combinator).

## Path matching

For path-matching we have the following directives:

* `pathEnd`
* `path(pathMatcher: PathMatcher)`
* `pathPrefix(pathMatcher: PathMatcher)`
* `extractUnmatchedPath`

### pathEnd

The directive will match only if the whole URI path has been matched.

### path(pathMatcher: PathMatcher)

This directive will match if the underlying pathMatcher matches the remaining of the URI path entirely.
This directive returns whatever the path matcher returns.

Examples:

```scala
path("user") 
// directive matches if the unmatched part of the URI path is just a single segment - /user
// this is a Directive0 as the constant string path matcher doesn't return a value
path("user" / segment)
// matches if the unparsed part of the URI path is a /user segment followed by a single segment
// this is a Directive1[String] because the 'segment' path matcher is a PathMatcher[String] 
```

### pathPrefix

Works almost the same as `path` but doesn't require the URI path to be fully matched by 
the underlying path matcher.

### extractUnmatchedPath

This is a `Directive1[List[String]]` - simply provides the unmatched part or the URI path, without "consuming" it.

## Path matchers

We have these simple path matchers:

* `segment: PathMatcher1[String]` - matches (and "consumes") a single segment of the URI path, and provides the segment as the value; rejects if there are no more unmatched segments left
* `segment(s: String): PathMatcher0` - matches only if the segment is equal to the provided string; doesn't provide a value; rejects if there are no more unmatched segments left  
* `regex(r: Regex): PathMatcher1[String]` - matches only if the segment matches the regular expression, provides the segment as the value; rejects if there are no more unmatched segments left
* `long: PathMatcher1[Long]` - matches if the segment can be parsed as a `Long`
* `double: PathMatcher1[Double]` - matches if the segment can be parsed as a `Double`

There is a couple of other path matchers provided, which are mostly intended to be used when defining custom path matchers:

* `fromTry[V](t: Try[V]): PathMatcher1[V]` - doesn't consume a segment from the URI path, but matches or rejects depending on whether the `Try` is successful, 
* `tryParse[V](t: => V): PathMatcher1[V]` - it wraps the computation of `t` in a `Try` and passed it to `fromTry`, effectively catching exceptions and rejecting if any  

Path matchers can be combined using the following combinators:

* `tmap[V: Tuple](f: T => V): PathMatcher[V]` - apply a transformation to the value; `Tuple` is a marker class from the [app.tulz.tuplez](https://github.com/tulz-app/tuplez/) lib 
  which makes sure instances are only defined for the tuples (1 to 22); 
  the reason for this is that `PathMatcher` internally always holds it's value as a tuple and this needs to be preserved 
* `tflatMap[V: Tuple](description: String)(f: T => PathMatcher[V])` - description here is needed because of how `frontroute` works under the hood;
* `tfilter(description: String)(f: T => Boolean): PathMatcher[T]`
* `tcollect[V: Tuple](description: String)(f: PartialFunction[T, V]): PathMatcher[V]`
* `/` - makes the matchers consume the URI path one after another: `path("user" / segment / "details" / segment) // : PathMatcher[(String, String)]` 
* `as[O: Tuple](f: T => O): PathMatcher[O]` - an alias for `map`
* `unary_! : PathMatcher[Unit]` - negates a matcher: `pathPrefix(!"users")`
* `PathMatcher.tprovide[V: Tuple](v: V): PathMatcher[V]` - always matches and provides a constant value
* `PathMatcher.provide[V](v: V): PathMatcher[Tuple1[V]]` - same as `tprovide`, but wraps the value into a `Tuple1`
* `PathMatcher.fail[T: Tuple](msg: String): PathMatcher[T]` - always fails
* `PathMatcher.unit: PathMatcher[Unit]` - always matches, doesn't provide any value

## Other directives (and "directives")

* `reject` - always rejects
* `param(name: String): Directive1[String]` - extracts the parameter value from the URI query string, fails if not present
* `maybeParam(name: String): Directive1[Option[String]]` - extracts the parameter value from the URI query string if present, always matches
* `tprovide[L: Tuple](value: L): Directive[L]` - always matches, providing a constant value
* `provide[L](value: L): Directive1[L]` - same as `provide`, wraps the value in `Tuple1`
* `debug(message: => String)(subRoute: Route): Route` - prints a debug message (using the `Logging` utility) whenever the route matches; !! make sure to check the `Debugging/logging` section below !! 



### Concat

* `concat(routes: Route*): Route` - allows to define alternative routes

We've seen it in action in the earlier examples.

### Directive combinators

Like patch matchers, directives provide a number of combinators.

* `tflatMap[R: Tuple](next: L => Directive[R]): Directive[R]`
* `tmap[R: Tuple](f: L => R): Directive[R]`
* `tcollect[R: Tuple](f: PartialFunction[L, R]): Directive[R]`
* `tfilter(predicate: L => Boolean): Directive[L]`

Unary directives (`Directive1`) also have the simpler `flatMap`, `map`, `collect` and `filter` combinators (without the `t` prefix and dealing with scalar values)


### Conjunction

Directives can be combined using the conjunction operator (`&`):

```scala
path("users" / segment / "password-reset") & param("code") // : Directive[(String, String)]
// will reject if any of the directives combined with & rejects
```

### Disjunction

Similarly, directives can be combined with an "or" operator (`|`):

```scala
pathEnd | path("index") // either will match


pathPrefix("users") {
  (pathEnd & param("userId") | path(segment)) { userIdFromPathOrFromParam => ...}
}

```

## The "signal" directive

One of the most interesting and powerful features in `frontroute` is the integration with Airstream signals.

There are two ways to use signals.

#### Convert a unary directive into a directive that provides a signal

If you have a unary directive (`Directive1`), you can call the `.signal` function on it.

* `.signal: Directive1[Signal[L]]`

It transforms a `Directive1[L]` into a `Directive1[Signal[L]]`.

When would this be useful?

Consider the following example:

```scala
pathPrefix("dashboard") {
  maybeParam("tab").map(_.getOrElse("summary")) { selectedTab => // : String 
    render(Page.Dashboard(selectedTab))   
  }
}
```

In this case, whenever the query string changes to have a different value for the `tab` parameter, the route will be re-evaluated and 
`render(Page.Dashboard(selectedTab))` will be called again. Depending on the way you implement your "actions" inside `complete`s, this might not be what you want.

For example, you might be re-rendering (to keep things simple) the whole page from scratch whenever `render` is called with a new `Page` value.

But in this case you might want to keep the rendered page and all the DOM and state, but change a visibility of some elements on the page according to
the `tab` parameter.


Now, if you use the `.signal` combinator:

```scala
pathPrefix("dashboard") {
  maybeParam("tab").map(_.getOrElse("summary")).signal { selectedTab => // : Signal[String] 
    render(Page.Dashboard(selectedTab))   
  }
}
```

the `selectedTab` will become a `Signal[String]`, and when the `tab` parameter changes, `render` will not be called again - but rather the value inside the
`selectedTab` signal will change. And you can react to it in your rendering logic.


#### Inject an external signal into the routing machinery

* `signal[T](signal: Signal[T]): Directive1[T]` - provide an external `Signal[T]` and get a `Directive[T]`

Whenever the underlying signal changes, the route will be re-evaluated. 

This can be useful for integrating with third-party libs, or, for example, to integrate you authentication mechanism with the routes:


```scala
val currentUser: Signal[Option[User]] = ???

def authenticatedUser: Directive1[User] =
  signal(currentUser).flatMap {
    case Some(a) => provide(a)
    case _       => reject
  }
  
val route =
  concat(
    pathPrefix("public") {
      ...
    },
    (pathPrefix("private") & authenticatedUser) { user => 
      ...
    }  
  )

```

### Debugging/logging

`frontroute` can provide a little bit of help when debugging you routes by logging the rejected logs.
By default the logger will not print anything (noop). To enable the `dom.console` output, set the
logger: `Logging.setLogger(Logger.consoleLogger)` (or implement your own).

The `debug` directive prints the messages using the same logger as well.


### A larger example 

```scala
import com.raquo.laminar.api.L._
import io.frontroute._

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
    
    runRoute(route, new BrowserRouteLocationProvider(windowEvents.onPopState))(unsafeWindowOwner)
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



![Documentation is WIP](https://img.shields.io/static/v1?label=Documentation&message=WIP&color=orange)

## An example project 

An example is available: https://github.com/yurique/laminar-router-example

