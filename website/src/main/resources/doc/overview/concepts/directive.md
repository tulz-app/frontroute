# Directive 

`Directive` is defined as follows:

```scala
class Directive[L](
  val tapply: (L => Route) => Route
)
```

Same as with the `Route`, it's unlikely that you'll need to work with this definition directly.

All the boilerplate is implemented in `frontroute`, so you'll be using the built-in directives and making your
custom directives using the combinators like `map` and `flatMap` on the existing ones.

Directives are inspired by the Akka HTTP directives, and are very similar to those.

Directives are used to build routes. We do this by calling the directive's `.apply` method and passing in a function 
that takes the output of the directive and returns the inner `Route`. 

Calling the `.apply` method looks like nesting in the code:

```scala
someDirective {  
 anotherDirective {
   // ...
 } 
}
```

With explicit `.apply` calls this would look like the following:

```scala
someDirective.apply { // this .apply takes the inner Route and returns a Route as well
 anotherDirective.apply { // this .apply returns a Route
   // ...
 } 
}
```

So there's no magic happening here.

## Directive's output

The purpose of a directive is to match against the current state of the routing (the unmatched path, 
query parameters, etc), and provide a value (as well as the updated state of the routing) to the "nested" route.   

Some directives have a single "output" value, some don't have output at all (`Unit`), 
and some have multiple outputs (tuples).

To have a better syntax when using directives, the constructor parameter is named `tapply`, while the `apply` method
(which calls `tapply` under the hood) is provided by the implicit conversion (extension methods). 

Directive's output type is defined by the `L` type parameter. 

For example, the `pathEnd` directive doesn't provide any output, so when nesting we are not "taking" any arguments:

```scala
pathEnd {
  // ...
}
```

Without the `apply` function being provided by extension methods, the above would have to be like this:

```scala
pathEnd { (_: Unit) =>
  // ...
}
```

Also, some directives provide tuples as their output (the most common case is multiple directives combined with `&`):

```scala
(pathEnd & param("param1") & param("param2")) { (param1, param2) => 
  // ...
}
```

Without the `apply` function being provided by extension methods, the above would have to be like this (note the `case`):

```scala
(pathEnd & param("param1") & param("param2")) { case (param1, param2) => 
  // ...
}
```

## Matching 

A directive's `.apply` method takes a function to build the nested route using the provided output of this directive (`L => Route`) 
and itself returns a `Route`.

`Route` is a function that accepts the current state of the routing, so when building the resulting `Route` a directive 
effectively has access to the state of the routing as well.

The state of the routing is defined as follows:

```scala
(RouteLocation, RoutingState, RoutingState) => EventStream[RouteResult]
```

Those `RoutingState`s are `fronroute`s internal state, and `RouteLocation` contains the following: 

* unmatched path
* query parameters
* history state
* other values parsed from the URL

## Directive builds a route 

A directive is defined by its `tapply`:

```scala
val tapply: (L => Route) => Route
```

Which means, when we call the directive's `apply` method we're building a [Route](/overview/route).

The way it works:

* a directive first tries to match against the current `RoutingState` and optionally extract some value
* if there is a match, it provides the extracted value to the function that builds an inner route

## Completing the route

Having only directives, we would not be able to ever complete building the route – to build a route using a directive we 
need to provide an inner route, and to build the inner route we would need to use another directive, and provide it with 
its inner route, etc.

So co complete the route there is a `complete` function:

```scala
def complete[T](events: EventStream[() => Unit]): Route
def complete[T](action: => Unit): Route
```

It returns a `Route` directly, using the provided action (or a stream of actions).
