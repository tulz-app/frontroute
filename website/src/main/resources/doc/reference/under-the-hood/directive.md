# Directive

Directives are used to build `Route`-s. 

A `Directive` class is defined as follows:

```scala
class Directive[L](
  val tapply: (L => Route) => Route
)
```

Which means a directive is an object that can build a `Route` by providing some value of type `L` to a function that can build a `Route`. 

Same as with `Route`-s, it's unlikely that you'll need to work with this definition directly.

The purpose of a directive is to match against the current routing state (path, query parameters, etc) and
extract a value of type `L`. 

We build a `Route` out of a directive by calling the directive's `.apply` method and passing in a function
that takes the output of the directive and returns a `Route`.

In the code, calling the `.apply` method looks like nesting :

```scala
someDirective {
  anotherDirective {
    ???
  }
}
```

With explicit `.apply` calls this would look like the following:

```scala
someDirective.apply { // this .apply takes the inner route as a parameter, and returns a route as well
  anotherDirective.apply { // this .apply returns a Route
    ???
  }
}
```

## Directive's output

Some directives have a "single" output value, some don't have output at all (`Unit`), and some have multiple outputs (
tuples).

To have a better syntax when using directives, the constructor parameter is named `tapply`, while the `.apply` method
(which in turn calls `tapply`) is provided by implicit conversions (extension methods).

For example, the `pathEnd` directive doesn't provide any output, so when nesting we are not "receiving" any values from
the directive:

```scala
pathEnd { ??? }
```

Without the `.apply` method being provided by extension methods, the above would have to be like this:

```scala
pathEnd.tapply { (_: Unit) => ??? }
```

Some directives provide tuples as their output (the most common case is multiple directives combined with `&`):

```scala
(pathEnd & param("param1") & param("param2")) { (param1, param2) => ??? }
```

Without the `apply` function being provided by extension methods, we would have to pattern match the tuple (note
the `case`):

```scala
(pathEnd & param("param1") & param("param2")).tapply { case (param1, param2) =>
  // ...
}
```

## Directive builds a route

A directive is defined by its `tapply`:

```scala
val tapply: (L => Route) => Route
```

What this means, is when we call a directive's `.apply` method (which in turn calls `tapply`) we're building
a [Route](/reference/under-the-hood/route).

The resulting route works this way:

* a directive first tries to match against the current `Location` and `RoutingState`, optionally extracting some
  information of type `L`;
* if it matches, it provides the extracted value of type `L` to the function that builds an inner route;
* then, the inner route is evaluated – it might have been built using a directive as well, in which case the process repeats recursively.

## Completing the route

Having only directives, we would not have been able to ever complete building a route: to build a route using a directive we
need to provide a way to build an inner route, and to build that inner route we would need to use another directive, and
provide it with its own inner route, etc. So we need a way to terminate this process somehow at the very "end".

For this, Laminar `Element`-s have an implicit conversion into a `Route`:

```scala
implicit def elementToRoute(e: => Element): Route
```

So we can complete building a route like this:

```scala
someDirective {
  anotherDirective {
    div("Render me!")
  }
}
```
