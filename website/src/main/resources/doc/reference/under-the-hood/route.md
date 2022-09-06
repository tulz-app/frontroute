# Route

`Route` is a `Modifier`, which wraps a function that accepts the current location,
the previous routing state and the current routing state:

```scala
trait Route extends ((RouteLocation, RoutingState, RoutingState) => Signal[RouteResult]) with Mod[HtmlElement]
```

The result of the `Route` function is either a match or a rejection.

```scala
object RouteResult {

  final case class Matched(state: RoutingState, location: RouteLocation, result: () => Signal[Element]) extends RouteResult

  case object Rejected extends RouteResult

}
```

When a route matches, it returns the remaining part of the location (which can subsequently be used in the
nested routes), the updated `RoutingState`, and the `Element` to be rendered (inside a `Signal`).

A `Route` acts like the `child.maybe <-- ...` modifier. When a `Route` is mounted, it subscribes to the relevant `Signal[Location]` 
and reacts to changes in it:

* if it's a match, the `Route` inserts the result into the parent element;
* if it's a rejection, it removes the previous result (if any) from the parent element;

There is a top-level location signal, which is derived directly from `window.location`, and changes
whenever a [popstate event](https://developer.mozilla.org/en-US/docs/Web/API/Window/popstate_event)
is emitted. Top-level routes will be subscribing to this signal.

Routes can (and often do) "consume" parts of the path in the location, leaving the remaining path for
the nested routes. Nested routes do not subscribe to the top-level location signal: rather, they get their own signals
with a location that may have been partially "consumed" by the parent routes.
