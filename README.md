# frontroute

![Maven Central](https://img.shields.io/maven—central/v/io.frontroute/frontroute_sjs1_2.13.svg) ![Scala.js](https://img.shields.io/static/v1?label=built+with&message=Scala.js&color=green)

`frontroute` is a front—end router library for single—page application (SPA) built with [Scala.js](http://www.scala—js.org/), with an API inspired by [Akka HTTP](https://doc.akka.io/docs/akka—http/current/).

Primarily designed for use with [raquo/Laminar](https://github.com/raquo/Laminar), though it's not a dependency and `frontroute` should fit nicely with any Scala.js library.

Built on top of:

* [raquo/Airstream](https://github.com/raquo/Airstream) `v0.11.1` 
* [tulz—app/tuplez](https://github.com/tulz—app/tuplez/) `v0.3.1`


### Getting started

`frontroute` is available for [Scala.js](http://www.scala—js.org/) v1.3.1+ (published for Scala 2.12 and 2.13).

```scala
libraryDependencies += "io.frontroute" %%% "frontroute" % "0.11.4"
```

```scala
import io.frontroute._
import io.frontroute.directives._
```

## Overview

Let's start with a small example:

```scala

val route =
  concat(
    pathEnd {
      // do something when the path is /
    },
    path("some—page") {
      // do something when the path is /some—page
    }
  )
```

`frontroute` is rather un—opinionated about the "do something" part in the above example, but more on that — below.

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
  pathPrefix("public") { // provides no value, Unit — no need to type "_ =>"
    concat( // not "nesting"
      pathPrefix("articles") { // no value
        path(segment) { articleId => // a String value provided 
          renderArticlePage(articleId)  
        }
      },
      (pathPrefix("books") & maybeParam("author") & maybeParam("title")) { 
        (maybeAuthor, maybeTitle) => 
          // no value from the pathPrefix, combined with Option[String] value from the first param directive and Option[String] from the second one
          // the internal value is a 2—tuple — (Option[String], Option[String])
          // but here, when nesting, you can provide either a single—parameter function that accepts the tuple — 
          //   Function1[  Tuple2[Option[String], Option[String]], ?  ]
          // or a 2—parameter function that accepts elements on the tuple —  
          //   Function2[  Option[String], Option[String], ?  ]
          // (this works with tuples of any size, see tuplez-apply)
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

But, most of the times, you will probably be using the provided `BrowserNavigation.locationProvider` — it takes a stream of `PopStateEvent` and 
parses the `dom.window.location` to produce the corresponding `RouteLocation`s.



```scala
val provideLocationProvider: RouteLocationProvider = BrowserNavigation.locationProvider(windowEvents.onPopState) // windowEvents.onPopState is available if you are using Laminar 
runRoute(route, routeLocationProvider)
BrowserNavigation.emitPopStateEvent() // this is most likely needed to force the first pop state event and make things happen 
```

`runRoute` also requires an implicit `Owner` (`unsafeWindowOwner` will work perfectly fine most of the times).

Under the hood,

* `runRoute` transforms a stream of `RouteLocation` (that you provide initially) into a stream 
of `() => Unit` functions (those functions are provided when defining the route; this is described below),
* it subscribes to this stream, and executes those functions as they come through,
* `runRoute` returns a `Subscription` which can be used to stop this process.

## Route actions

So far we haven't touched on how to actually do anything when a particular route is matched. 

Let's look at another small example:


```scala
concat(
  path("page—1") {
    // do something when the path is /page—1
  },
  path("page—2") {
    // do something when the path is /page—2
  }
)
```

### The "complete" directive

In a simple case, the "do something" here means "execute some code" (do `console.log`, update the "current page" signal , etc).

For that, there is a built—in directive — `complete`.

> Side note: it is not really a directive, but rather a function that terminates a 
tree of directives (by returning a `Route` which eventually gets used to build the root `Route`).
> But that is not important from a user's stand—point, 
and it might be simpler to think about "complete" as of just another directive. 

It accepts a by—name block of code that will get executed whenever the route is matched:

```scala
def complete[T](action: => Unit): Route
```

The example would look like the following:

```scala
concat(
  path("page—1") {
    dom.console.log("page—1 route matched")
  },
  path("page—2") {
    dom.console.log("page—2 route matched")
  }
)
```

### The "completeN" directive

A more powerful version of `complete` is `completeN`:

```scala
def completeN[T](events: EventStream[() => Unit]): Route
```

* `completeN` accepts a stream of `() => Unit` functions

When the route is matched, `frontroute` subscribes to this stream and "executes" the functions emitted by the stream.

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
  path("page—1") {
    render(Page.Page1)
  },
  path("page—2") {
    render(Page.Page2)
  }
)
```

### A more complicated use case

If you wanted to get some data from the back—end before rendering a page, you could use `completeN`.
Also, this will prevent "this" action from taking effect when the call to the back—end returns after 
the route has changed and another "complete" is in effect.

For example, let's say we want to be displaying a "loading" screen while the data is being requested and after that — the actual page.

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

The good thing, though, is you will most likely not need to do anything low—level — most directives are supposed to be built from the 
existing directives using the combinators.

Say, you wanted to check if the matched segment (part of the URI path delimited by `/`) is a number. 

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
  def isNumber(s: String): Directive[Int] = 
    Try(s.toInt) match {
      case Success(int) => provide(int)
      case Failure(_) => reject
    }  
```

Now, this is an extemely simple example, but most directives, no matter how complicated, can be
defined as easily from the more powerful existing directives and more powerful combinators (like, for example, the `signal` directive or the `flatMap` combinator).

## Path matching

When evaluating the route tree `frontroute` keeps and updates its internal state, which includes the "unmatched path". 

Unmatched path is a essentially a `List[String]`, and is initially set to `location.pathname.dropWhile(_ == '/').split('/').toList.dropWhile(_.isEmpty)`.

For example, when the path is `/users/12/posts/43/details` the initial "unmatched path" is set to `List("users", "12", "posts", "43", "details")`.

When one of the path matching directives matches, it "consumes" the part of the "unmatched path" 

> It is actually the `PathMatcher` provide to the directive that does the matching and "consuming".

For example, with the above initial "unmatched path", here's what the "unmatched path" will be during the route evaluation:

```scala
unmatchedPath: List("users", "12", "posts", "43", "details")
concat(
  
  unmatchedPath: List("users", "12", "posts", "43", "details")
  // "public" != "users"
  //   ——> rejects
  //   ——> directive rejects
  pathPrefix("public") { ... }, 
  
  unmatchedPath: List("users", "12", "posts", "43", "details")
  // "users" == "users"
  //   ——> matches, provides Unit
  //   ——> "users" is consumed (unmatchedPath: List("12", "posts", "43", "details"))
  // "all" != "12" 
  //   ——> rejects 
  //   ——> " ... / ... " rejects 
  //   ——> unmatchedPath is rolled back
  //   ——> directive rejects
  pathPrefix("users" / "all") { userId => 
    // route evaluation never reaches here 
    pathPrefix("something") { ... }
  },

  unmatchedPath: List("users", "12", "posts", "43", "details")
  // "users" == "users" 
  //   ——> matches and provides Unit
  //   ——> "users" is consumed (unmatchedPath: List("12", "posts", "43", "details"))
  // segment matches any string 
  //   ——> matches and provides "12"
  //   ——> "12" is consumed (unmatchedPath: List("posts", "43", "details"))
  //   ——> " ... / ... " matches, Unit and "12" are combined into just "12"
  //   ——> provides "12"
  //   ——> directive matches and provides "12"
  pathPrefix("users" / segment) { userId => // userId == "12"
    unmatchedPath: List("posts", "43", "details")
    // "posts" == "posts"
    //   ——> matches and provides Unit
    //   ——> "posts" is consumed (unmatchedPath: List("43", "details"))
    //   ——> directive matches and provides Unit
    pathPrefix("posts") {
      unmatchedPath: List("43", "details")
      concat(
        unmatchedPath: List("43", "details")
        // "all" != "43" 
        //   ——> rejects
        //   ——> directive rejects
        path("all") { ... },
        
        unmatchedPath: List("43", "details")
        // long matches "43"
        //   ——> matches and provides 43: Long
        //   ——> "43" is consumed (unmatchedPath: List("details"))
        //   ——> directive matches and provides 43: Long
        pathPrefix(long) { postId => // postId: Long == 43
          unmatchedPath: List("details")
          // no match 
          //   ——> rejects
          //   ——> directive rejects
          pathEnd { ... },
          
          unmatchedPath: List("details")
          // "details" == "details" AND no more unmatched segments
          //   ——> matches and provides Unit
          //   ——> "details" is consumed (unmatchedPath: List.empty)
          //   ——> directive matches and provides Unit
          path("details") { 
            unmatchedPath: List.empty
            // complete terminates the evaluation, the provided code block will get executed
            complete {
              dom.console.log("user post details — match")
            }
          }
        }
      )      
    }
  }
)

```

For path—matching we have the following directives:

### `pathEnd: Directive0`

The directive will match only if the whole URI path has been matched.

### `pathPrefix[T](m: PathMatcher[T]): Directive[T]`

This directive will match if the underlying pathMatcher matches at the beginning of the unmatched path.
This directive returns whatever the path matcher returns.

Examples:

```scala
pathPrefix("user") 
// directive matches if the "unmatched path" starts with a "user" segment
// this is a Directive0 as the constant string path matcher doesn't return a value

pathPrefix("user" / segment)
// matches if the "unmatched path" starts with a "user" segment followed by another segment
// this is a Directive[String] because the 'segment' path matcher is a PathMatcher[String] 
```

### `path[T](m: PathMatcher[T]): Directive[T]`

Works almost the same as `pathPrefix` but requires the "unmatched path" to be empty after the segments are consumed. Effectively, it is a `pathPrefix(matcher)` followed by the `pathEnd`

### `extractUnmatchedPath: Directive[List[String]]`

Always matches, provides the "unmatched path" without "consuming" it.

## Path matchers

We have these simple path matchers:

* `segment: PathMatcher[String]` — matches (and "consumes") a single segment of the URI path, and provides the segment as the value; rejects if there are no more unmatched segments left
* `segment(s: String): PathMatcher0` — matches only if the segment is equal to the provided string; doesn't provide a value; rejects if there are no more unmatched segments left; `PathMatcher0` is `PathMatcher[Unit]`  
* `regex(r: Regex): PathMatcher[String]` — matches only if the segment matches the regular expression, provides the segment as the value; rejects if there are no more unmatched segments left
* `long: PathMatcher[Long]` — matches if the segment can be parsed as a `Long`
* `double: PathMatcher[Double]` — matches if the segment can be parsed as a `Double`

There is a couple of other path matchers provided, which are mostly intended to be used when defining custom path matchers:

* `fromTry[V](t: Try[V]): PathMatcher[V]` — doesn't consume a segment from the URI path, but matches or rejects depending on whether the `Try` is successful, 
* `tryParse[V](t: => V): PathMatcher[V]` — it wraps the computation of `t` in a `Try` and passed it to `fromTry`, effectively catching exceptions and rejecting if any  

Path matchers can be combined using the following combinators:

* `map[V](f: T => V): PathMatcher[V]` — apply a transformation to the value   
* `flatMap[V](description: String)(f: T => PathMatcher[V])` — description here is needed because of how `frontroute` works under the hood
* `filter(description: String)(f: T => Boolean): PathMatcher[T]`
* `collect[V](description: String)(f: PartialFunction[T, V]): PathMatcher[V]`
  ```scala
  path("user" / segment / "details" / segment) // : PathMatcher[(String, String)]
  ``` 
* `as[O](f: T => O): PathMatcher[O]` — an alias for `map`
* `unary_! : PathMatcher[Unit]` — negates a matcher: `pathPrefix(!"users")`
* `PathMatcher.provide[V](v: V): PathMatcher[V]` — always matches and provides a constant value
* `PathMatcher.fail[T](msg: String): PathMatcher[T]` — always fails
* `PathMatcher.unit: PathMatcher[Unit]` — always matches, doesn't provide any value

## Other directives (and "directives")

* `reject` — always rejects
* `extractHostname: Directive[String]` — provides `window.location.hostname`
* `extractPort: Directive[String]` — provides `window.location.port`
* `extractHost: Directive[String]` — provides `window.location.host` (`hostname:port`)
* `extractProtocol: Directive[String]` — provides `window.location.protocol`
* `extractOrigin: Directive[Option[String]]` — provides `window.location.origin`
* `param(name: String): Directive[String]` — extracts the parameter value from the URI query string, fails if not present
* `maybeParam(name: String): Directive[Option[String]]` — extracts the parameter value from the URI query string if present, always matches
* `provide[L](value: L): Directive[L]` — always matches, providing a constant value
* `debug(message: => String)(subRoute: Route): Route` — prints a debug message (using the `Logging` utility) whenever the route matches; !! make sure to check the `Debugging/logging` section below !! 
* `historyState: Directive[Option[js.Any]]` — extracts the history state (will only work if `BrowserNavigation` is used for `pushState`/`replaceState`); `BrowserNavigation` is described below 
* `historyScroll: Directive[Option[ScrollPosition]]` — if `BrowserNavigation` is used `pushState`/`replaceState` it can preserve the window scroll position when navigating (enabled by default); this directive returns the preserved window scroll position (if any) 

### Concat

* `concat(routes: Route*): Route` — allows to combine alternate routes.

All the provided routes will be "tried" sequentially until one of them matches. If all routes reject, the `concat` route rejects as well.

We've seen it in action in the earlier examples.

### Directive combinators

Like patch matchers, directives provide a number of combinators.

The basic ones are:

* `flatMap[R](next: L => Directive[R]): Directive[R]`
* `map[R](f: L => R): Directive[R]`
* `collect[R](f: PartialFunction[L, R]): Directive[R]`
* `filter(predicate: L => Boolean): Directive[L]`

### Conjunction

Directives can be combined using the conjunction operator (`&`):

```scala
path("users" / segment / "password—reset") & param("code") // : Directive[(String, String)]
```

In the above example, the conjunction will reject if any of the parts rejects.

If all parts match, the conjunction:
* matches as well
* the values provided by the parts are combined into a single n—tuple (or a scalar), omitting `Unit`s 
* and the resulting tuple (or a scalar) is provided by the conjunction directive 
* if all parts provide Unit — the conjunction will provide Unit as well 
* see the [app.tulz.tuplez](https://github.com/tulz—app/tuplez/) lib for details


### Disjunction

Similarly, directives can be combined with an "or" operator (`|`):

```scala
pathEnd | path("index") // either will match


pathPrefix("users") {
  (pathEnd & param("userId") | path(segment)) { userIdFromPathOrFromParam => ...}
}
```

The disjunction will match if any of the parts matches. Rejects if all parts reject.
Provides the value provided by the first part that matched (other parts are not evaluated in that case).

## The "signal" directive

One of the most interesting and powerful features in `frontroute` is the integration with Airstream signals.

There are two ways to use signals.

#### Convert a unary directive into a directive that provides a signal

Whenever you have a directive, you can call the `.signal` function on it.

* for example `param("param-name").signal // : Directive[Signal[String]]`

It transforms any `Directive[L]` into a `Directive[Signal[L]]`.

When would this be useful?

Consider the following example:

```scala
pathPrefix("dashboard") {
  maybeParam("tab").map(_.getOrElse("summary")) { selectedTab => // : String 
    render(Page.Dashboard(selectedTab))   
  }
}
```

In this case, whenever the query string changes to have a different value for the `tab` parameter, the route will be re—evaluated and 
`render(Page.Dashboard(selectedTab))` will be called again. Depending on the way you implement your "actions" inside `complete`s, this might not be what you want.

For example, you might be re—rendering (to keep things simple) the whole page from scratch whenever `render` is called with a new `Page` value.

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

the `selectedTab` will become a `Signal[String]`, and when the `tab` parameter changes, `render` will not be called again — but rather the value inside the
`selectedTab` signal will change. And you can react to it in your rendering logic.


#### Inject an external signal into the routing machinery

* `signal[T](signal: Signal[T]): Directive[T]` — provide an external `Signal[T]` and get a `Directive[T]`

Whenever the underlying signal changes, the route will be re—evaluated. 

This can be useful for integrating with third—party libs, or, for example, to integrate you authentication mechanism with the routes:


```scala
val currentUser: Signal[Option[User]] = ???

def authenticatedUser: Directive[User] =
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

### Navigation (History API)

`frontroute` uses (and depends on, in order to work correctly) the [History API](https://developer.mozilla.org/en—US/docs/Web/API/History).

`BrowserNavigation` object is provided and should be used for all navigation.

#### BrowserNavigation.locationProvider

```scala
def locationProvider(popStateEvents: EventStream[dom.PopStateEvent]): RouteLocationProvider
```

Creates an instance of `RouteLocationProvider` that is required to run the routes (see description above).
It takes a single parameter — a stream of `PopStateEvent`. If you're using Laminar, this stream is provided by `windowEvents.onPopState`.

#### BrowserNavigation.preserveScroll

```scala
def preserveScroll(keep: Boolean): Unit
```

Configures whether `BrowserNavigation` should preserve the window scroll location (in history state) when pushing state (`pushState`).

#### emitPopStateEvent

```scala
def emitPopStateEvent(): Unit
```

Emits (`dom.window.dispatchEvent`) a `popstate` event. You will most likely need to call this right after calling `runRoute`.

#### restoreScroll

```scala
def restoreScroll(): Unit
```

If scroll position is available in the current history state — scrolls the window to that position. You might want to use this after you render your content and want the `back`/`forward` buttons 
to get the user to the position on the page where they used to be before navigation.

#### pushState / replaceState

```scala
def pushState(
  data: js.Any = js.undefined,
  title: String = "",
  url: js.UndefOr[String] = js.undefined,
  popStateEvent: Boolean = true
): Unit

def replaceState(
  url: js.UndefOr[String] = js.undefined,
  title: String = "",
  data: js.Any = js.undefined,
  popStateEvent: Boolean = true
): Unit
```

These functions should be used for navigation instead of directly calling `window.history.pushState` / `window.history.replaceState`.

If `popStateEvent` is `true`, `emitPopStateEvent` will be called right after `window.history.pushState` / `window.history.replaceState` 
(the browser does not emit this event in case of programmatic history push/replace, set it to `false` only if you know what you are doing).


### Debugging/logging

`frontroute` can provide a little bit of help when debugging you routes by logging the rejected logs.
By default the logger will not print anything (noop). To enable the `dom.console` output, set the
logger: `Logging.setLogger(Logger.consoleLogger)` (or implement your own).

The `debug` directive prints the messages using the same logger as well.

### `LinkHandler`

You can call `LinkHandler.install()` at the app start:

```scala
io.frontroute.LinkHandler.install()
```

It registers a click handler for all `<a>` elements on the page (existing and future):
* when `rel` is empty or not set – calls `BrowserNavigation.pushState` with the anchor's `href`
* when `rel` is `external` – opens the anchor's `href` in a new tab (`dom.window.open(anchor.href)`)
* when `res` has a different non-empty value – the click event is propagated.

It also registers a global `window.routeTo` function with one parameter – `path`, which calls `BrowserNavigation.pushState(path = path)`.

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
            path("page—1") {
                completeRender {
                  Page1()
                }
            },
            path("page—2") {
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
    
    runRoute(route, BrowserNavigation.locationProvider(windowEvents.onPopState))(unsafeWindowOwner)
    BrowserNavigation.emitPopStateEvent() 
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
      onClick.preventDefault ——> { _ =>
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
        navLink("/pages/page—1", "Page 1"),
        navLink("/pages/page—2", "Page 2")
       ),
      div(
        child <—— $child
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

An example is available: https://github.com/yurique/laminar—router—example



## Author

Iurii Malchenko – [@yurique](https://twitter.com/yurique) / [keybase.io/yurique](https://keybase.io/yurique)


## License

`frontroute` is provided under the [MIT license](https://github.com/tulz—app/frontroute/blob/main/LICENSE.md).

