# Route

`Route` is a `Modifier`, which wraps a function that accepts the current unmatched location,
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

When a route function matches, it provides the remaining part of the location (which can subsequently be used in the
nested routes), and the `Element` to be rendered (inside a `Signal`).

A `Route` acts like the `onMountInsert` modifier.
It subscribes to the `Signal` of the current unmatched location and applies the route function
to the values in that signal:

* if it's a match, the `Route` inserts the result into the element the router is applied to;
* if it's a rejection, it removes the previous result (if any) from the element;

The current unmatched location can be either the full location (if it's a top-level route), or the remaining part of
a location provided by the nearest router above the current one.
