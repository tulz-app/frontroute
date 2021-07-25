## How to use `frontroute`

* define a [route](/overview/route) using [directives](/overview/directive)
* get a location provider
* pass the route and the location provider to the `runRoute` function

> #### Location Provider
>
> Location provider a lightweight abstraction over the current URL in the browser and the events of this URL being
changed (`EventStream[RouteLocation]`).
>
> [More about location providers.](/overview/location-provider)

## How it works

When we call `runRoute`:
* it subscribes to the event stream provided by the location provider
* whenever that stream emits, it runs the route against the current location 
* `Route` is a function that returns a [RouteResult](/reference/route-result) – either a "completion" or a "rejection"
* if the route rejects – nothing happens
* if the route completes – the actions it completes with get executed 

### Basic example

```scala
import com.raquo.laminar.api.L._
import io.frontroute._
import org.scalajs.dom.console

val route = path("some-page") {
  complete {
    console.log("some-page!")
  }
}

val locationProvider = LocationProvider.browser(windowEvents.onPopState)
runRoute(route, locationProvider)(unsafeWindowOwner)
```

Here we are using a simple directive – `path` and the `complete` function in order to build the route:

* `path("some-page")` is a directive
* `complete { console.log("some-page!") }` returns a route that completes unconditionally with the given action (`console.log("some-page!")` in this case) 
* we call the `.apply` method of the `path("some-page")` directive and provide the route returned by the `complete`
* `path("some-page").apply` builds a new route, which completes conditionally – only when the unmatched path is equal to `/some-page`


## Alternative routes

```scala
import com.raquo.laminar.api.L._
import io.frontroute._
import org.scalajs.dom.console

val route = concat(
  path("some-page") {
    complete {
      console.log("some page!")
    }
  },
  path("another-page") {
    complete {
      console.log("another page!")
    }
  }
)

val locationProvider = LocationProvider.browser(windowEvents.onPopState)
runRoute(route, locationProvider)(unsafeWindowOwner)
```

Here we're using a new function – [concat](/reference/concat). 

`concat` takes a list of alternative routes and returns a new route (lets call it `concat-route`):

* if any of the alternative routes completes (or order) – `concat-route` will complete with the same result 
* if all the alternative routes reject – `concat-route` will reject as well

## Route completion

When a route matches, it provides an action to be executed (a stream of actions, to be precise).

An action is just a `() => Unit` function, so what it does is completely up to the author of the route.

In the examples above, we had actions that printed something to the console. 

In the real application you will probably want to have a signal with a description of what should be rendered. Thus your
actions would be doing something like `pageToBeRendered.writer.onNext(...)`.

For such a common use case there is a built-in helper function in `frontroute`.

## makeRoute

```scala
import io.frontroute._
import com.raquo.laminar.api.L._

// renders is a Signal[Option[HtmlElement]]
// route is a Route
val (renders, route) = makeRoute[HtmlElement] { render =>
  concat(
    pathEnd {
      render { div("path is /") }
    },
    path("new-path") {
      render { div("path is /new-path") }
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

`runRoute[T]` returns a tuple: `(Signal[Option[T]], Route)`. It provides you with a `render` function, that you call in order to set the values of the signal.

In the above example, whenever `render { div("path is /") }` happens, `renders` current value will be set to `Some(div("path is /"))`.

## Path-matching

For path-matching we have the following directives:

* [pathEnd](/reference/path-end)
* [pathPrefix](/reference/path-prefix)
* [path](/reference/path)

`path` and `pathPrefix` take a [PathMatcher](/overview/path-matcher) as an argument.

The simplest way is to use strings as path matchers (strings are implicitly converted into path matchers): 

```scala
path("some-page")
pathPrefix("some-section")
```

For other matchers see the [reference](/reference/path-matchers). 

## Further reading

It is highly recommended getting familiar with the concept of the [directives](/overview/directive).

