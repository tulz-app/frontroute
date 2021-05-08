# Route

`Route` is defined as follows:

```scala
type Route = (RouteLocation, RoutingState, RoutingState) => EventStream[RouteResult]
```

We build `Route`s using [directives](/overview/directive).

A `Route` is a function that accepts the current state of the routing process and returns a stream of
route results.

Route result is either a "completion with a stream of actions" or a rejection.

Action is a `() => Unit`.

This is an implementation detail, and you will most likely never need to work with it directly.

