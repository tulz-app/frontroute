# frontroute

![Maven Central](https://img.shields.io/maven-central/v/io.frontroute/frontroute_sjs1_2.13.svg) ![Scala.js](https://img.shields.io/static/v1?label=built+with&message=Scala.js&color=green)

`frontroute` is a front-end router library for single-page applications (SPA) built with [Scala.js](http://www.scala-js.org/), with an API inspired by [Akka HTTP](https://doc.akka.io/docs/akka-http/current/).

`frontroute` is primarily designed for use with [raquo/Laminar](https://github.com/raquo/Laminar), 
but it doesn't have Laminar as a dependency and should fit nicely with any Scala.js library.

Built on top of:

* [raquo/Airstream](https://github.com/raquo/Airstream) `v0.12.2` 
* [tulz-app/tuplez](https://github.com/tulz-app/tuplez) `v0.3.5`

## Example project

An example is available here: https://github.com/yurique/frontroute-example

### Getting started

`frontroute` is available for [Scala.js](http://www.scala-js.org/) `v1.5.0`+ (published for Scala 2.12 and 2.13).

```scala
libraryDependencies += "io.frontroute" %%% "frontroute" % "0.12.2"
```

For Airstream `v0.11.x`:

```scala
libraryDependencies += "io.frontroute" %%% "frontroute" % "0.11.7"
```

```scala
import io.frontroute._
```

## Overview

Let's start with a small example:

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

`frontroute` is rather un-opinionated about the "do something" part in the above example, but more on that — below.

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
* `provide`
* `signal`

One can define most of the routing for the app using those, but it is possible (and encouraged) to implement custom directives.

Directives are designed to be nested (as well as combined with `&` and `|`). 

Whenever a directive "matches", it gives control to the nested directive, providing the value it has "extracted":

```scala
concat( // this is not "nesting", unlike most of the following "calls"
  pathPrefix("public") { // provides no value – Unit — thus no need to type "_ =>"
    concat( // not "nesting"
      pathPrefix("articles") { // no value
        path(segment) { articleId => // a String value provided 
          renderArticlePage(articleId)  
        }
      },
      (pathPrefix("books") & maybeParam("author") & maybeParam("title")) { 
        (maybeAuthor, maybeTitle) => 
          // * no value from the pathPrefix,
          // * combined with Option[String] value from the first param directive 
          // * combined with Option[String] from the second one
          //
          // the internal value is a 2—tuple — (Option[String], Option[String])
          // 
          // but here, when nesting, you can provide either a single-parameter function that accepts the tuple — 
          //   Function1[  Tuple2[Option[String], Option[String]], ?  ]
          //
          // or a 2-parameter function that accepts elements on the tuple —  
          //   Function2[  Option[String], Option[String], ?  ]
          //
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

you need to run the `runRoute` function, providing the route and an instance of `LocationProvider`. 

`LocationProvider` is a simple trait:

```scala
trait LocationProvider {
  def stream: EventStream[RouteLocation]
}
```

and its single job is to provide a stream of `RouteLocation`. You can implement it depending on your needs (for tests,
for example), but most of the time you will probably be using the provided `LocationProvider.browser` — it takes 
a stream of `PopStateEvent` and parses the `dom.window.location` to produce the corresponding `RouteLocation`s.

```scala
val locationProvider: LocationProvider = LocationProvider.browser(windowEvents.onPopState) // windowEvents.onPopState is available if you are using Laminar 
runRoute(route, locationProvider)
BrowserNavigation.emitPopStateEvent() // this is most likely needed to force the initial pop state event and make things happen 
```

`runRoute` also requires an implicit `Owner` (`unsafeWindowOwner` will work perfectly fine most of the time).

Under the hood

* `runRoute` transforms a stream of `RouteLocation` (that you provide initially) into a stream 
  of `() => Unit` functions (those functions are provided when defining the route; this is described below),
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

In a simple case, the "do something" here means "execute some code" (do `console.log`, update the "current page" signal, etc).

For that, there is a built-in directive — `complete`.

> Side note: it is not really a directive, but rather a function that terminates a 
tree of directives (by returning a `Route`, which eventually gets used to build the root `Route`).
> But that is not important from a user's standpoint, 
and it might be simpler to think about "complete" as of just another directive. 

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

There is a more powerful version of `complete`:

```scala
def complete[T](events: EventStream[() => Unit]): Route
```

This overload of `complete` accepts a **stream** of `() => Unit` functions.

When the route is matched, `frontroute` subscribes to this stream and "executes" the functions emitted by the stream.

As soon as the route changes (another `complete` is "triggered"), this subscription gets cancelled.

### How to use the "complete" directive

Let's look at an example of how you can do something useful with `complete`. 

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

You might define a custom "directive" (again, it will not be a real directive as we're going to build it 
on top of `complete`) that updates the value of the `currentPage`:

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

### `makeRoute` 

For cases like the one described above, there are two utility functions available:

* `makeRoute[A](make: (A => Route) => Route): (Signal[Option[A]], Route)`
* `makeRouteWithCallback[A](onRoute: () => Unit)(make: (A => Route) => Route): (Signal[Option[A]], Route)`

Both return a tuple of a `Signal[Option[A]]` (where `A` is what you want to be a "result" of routing) and a `Route` which
can be passed to the `runRoute`.

`makeRouteWithCallback` accepts an additional parameter, `onRoute: () => Unit`, which will be called every time a route changes
(and matches). Otherwise it works the same way as the `makeRoute` does.

When using `makeRoute`, you build the route as you would without it, but it provides you with a "render" function, which you 
call instead of `complete` and provide a value that ends up in the resulting signal (inside `Some()`).

The example from the previous section can be rewritten as follows (traits and case classes are omitted for brevity):

```scala
val (routeResult, route) = makeRoute { render =>
  concat(
    path("page-1") {
      render(Page.Page1)
    },
    path("page-2") {
      render(Page.Page2)
    }
  )
}
val currentPage = routeResult.map(_.getOrElse(Page.Blank))
// runRoute(route) ...
```

### A more complicated use case

If you wanted to get some data from the back-end before rendering a page, you could use `complete` that accepts a stream 
of functions.

This will also prevent "this" action from taking effect when the call to the back-end returns **after** 
the route has changed and another `complete` is "in effect".

For example, let's say we want to be displaying a "loading" screen while the data is being requested, and after that — 
the actual page.

```scala
// ...
final case object Loading extends Page
final case class UserPage(data: UserData) extends Page
// ...
```

```scala
def renderF(pageFuture: => Future[Page]): Route =
  complete {
    EventStream.merge(
      EventStream.fromValue(
        () => currentPage.writer.onNext(Page.Loading) 
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
      API.getUserData(userId)/*: Future[UserData]*/.map ( userData => 
        Page.UserPage(userData)
      )      
    )
  }
  // ...
)
```

As a side note, in order to keep the route readable as it grows, it is recommended to extract 
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




### `path[T](m: PathMatcher[T]): Directive[T]`

Works almost the same as `pathPrefix` but requires the "unmatched path" to be empty after the segments are consumed. Effectively, it is a `pathPrefix(matcher)` followed by the `pathEnd`




![Documentation is WIP](https://img.shields.io/static/v1?label=Documentation&message=WIP&color=orange)

## Author

Iurii Malchenko – [@yurique](https://twitter.com/yurique) / [keybase.io/yurique](https://keybase.io/yurique)


## License

`frontroute` is provided under the [MIT license](https://github.com/tulz-app/frontroute/blob/main/LICENSE.md).

