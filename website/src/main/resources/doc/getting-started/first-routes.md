### Routes

`frontroute`'s `Route`-s are regular Laminar modifiers, and they act similarly to the `child.maybe <-- ...` modifier:

* when there is a "match", the corresponding element is inserted into the parent,
* when there is no "match", the element is removed.

And because routes are just modifiers, all we need to do is put them inside our view tree:

```scala
div(
  pathEnd {
    div("Path is /")
  }
)
```

<div class="bg-blue-100 p-2 text-sm">

Under the hood, a route is defined by a function, with a signature that approximately looks like
this: `Signal[Location] => Option[Element]`
(the actual signature is a bit more complicated, but that is an implementation detail, and we will not be dealing with
it directly). `Location` is an equivalent of the
[Window.location](https://developer.mozilla.org/en-US/docs/Web/API/Window/location). Because we're in a single page
application, and `Window.location` can change dynamically, the "current location" is modeled as a `Signal[Location]`.

When a `Route` is mounted, it subscribes to the relevant `Signal[Location]` and reacts to changes in it.

There is a top-level location signal, which (by default) is derived directly from `Window.location`, and changes
whenever a [popstate event](https://developer.mozilla.org/en-US/docs/Web/API/Window/popstate_event)
is emitted in the DOM. Top-level routes will be subscribing to this signal.

Routes can (and often do) "consume" parts of the path in the location, leaving the remaining path ("unmatchedPath") for
the nested routes. Nested routes do not subscribe to the top-level location signal: rather, they get their own signals
with a location that may have been partially "consumed" by the parent routes.

</div>

Let's build a simple app with routes:

```scala
import com.raquo.laminar.api.L.*
import io.frontroute.*

val myApp: Element =
  div(
    pathEnd {
      div("Path is /")
    },
    path("my-path") {
      div(s"Path is /my-path")
    },
    noneMatched {
      div("Path is something else")
    }
  )
```

Here we've defined the following three routes:

* `pathEnd { ... }`,
* `path("my-path") { ... }`, and
* `noneMatched { ... }`.

When we mount `myApp`, one of the three nested `div()` elements will be rendered – depending on the current path in the
browser.

#### Routes and Directives

Routes are defined using directives.

<div class="bg-blue-100 p-2 text-sm">

`frontroute` routes and directives are inspired
by [Akka HTTP routing DSL](https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/index.html).

</div>

In the example above, there are three directives: `pathEnd`, `path`, and `noneMatched`. Directives alone are not routes,
nor they are modifiers, and cannot be applied to elements. The following will not compile (nor does it have any
meaning):

```scala
div(
  pathEnd
)
```

The role of a directive is to do the actual matching of the `Location`. While matching, directives also "extract"
information from the location (or from [other sources](/reference/injecting-external-signal)) and provide it to the
rendered element.

In order to get a `Route`, we need to also provide an `Element` that will be rendered when the directive matches. This
is done by calling the `.apply` method on the directive. With `.apply` spelled out explicitly, the `pathEnd { ... }`
route from the above example would look like this:

```scala
pathEnd.apply {
  div("Path is /")
}
```

Directives have a type parameter which describes the value that the directive will extract.

The three directives we've seen so far happen to not extract anything and have type `Directive[Unit]` (aliased
as `Directive0`). The syntax for such directives might have looked like the following:

```scala
pathEnd { (_: Unit) =>
  div("Path is /")
}
```

But because these directives are so common, and having to put `_ => ` can get annoying quickly (and it doesn't look very
nice), we have a special syntax for `Directive0`-s, which allows us to pass an element by-name (like we were doing in
the examples above):

```scala
pathEnd {
  div("Path is /")
}
```

For directives that **do** extract (and provide) a value, instead of a by-name element, we provide a function that
accepts the output of the directive and returns an element:

```scala
div(
  path(segment) { matchedSegment =>
    div(s"The path is /$matchedSegment")
  }
)
```

In this example, `path(segment)` is a `Directive[String]`, so we pass a `String => Element` function to the
directive's `.apply` method. This function will be called every time the extracted value changes (and it will **not** be
called if the location changes, but the extracted value remains the same).

<div class="bg-blue-100 p-2 text-sm">

We have seen the `path` directive already, in the first example: `path("my-path")`. There, it was a `Directive[Unit]`.
In this case, `path(segment)` is a `Directive[String]`. This is because the output of path-matching directives is
defined by the "path matcher" that we use:

* `"my-path"` is implicitly converted into a `PathMatcher[Unit]`, which checks if the "unmatchedPath" contains a single
  segment and if it's equal to `"my-path"`, and does not extract anything;
* `segment` is built-in path matcher, which checks if the "unmatchedPath" contains a single segment, and extracts that
  segment as its output.

Thus, `path("my-path")` is a `Directive[Unit]`, and `path(segment)` is a `Directive[String]`.

</div>

#### Nesting directives

The description above is not 100% accurate, though: the `.apply` method of a directive accepts not a by-name element or
a function that returns an element: it accepts a `Route` (or a function that returns a `Route`). Rather, elements are
implicitly converted into `Routes` (to enable the simplified syntax: we don't have to use – or have – the `complete`
function, like we have in Akka HTTP or in earlier versions of `frontroute`).

This enables us to "nest" directives:

```scala
pathPrefix("posts") {
  path(segment) { postId =>
    div("Post ID: $postId")
  }
}
```

Here, the `pathPrefix("posts")` directive will be executed first. When it matches, it will run the inner route
– `path(segment) { ... }`. The inner route will "see" the location with the "posts" segment removed from the path – it
will have been "consumed" by the `pathPrefix("posts")` directive.

<div class="bg-blue-100 p-2 text-sm">

This looks and behaves very similar to nested routes:

```scala
pathPrefix("posts") {
  div(
    path(segment) {
      postId
      div("Post ID: $postId")
    }
  )
}
```

The difference is that in case of "nested directives", there will be a single modifier applied to the element and a
single subscription to the location signal that will drive the whole "tree of routes".
</div>

#### Combining directives

Nesting is one way of combining directives. We also have conjunction (`directive1 & directive2`) and
disjunction (`directive1 | directive2`).

<div class="bg-blue-100 p-2 text-sm">

Directives are monads, so they also have `.map` and `.flatMap`. This is covered in
the [custom directives](/overview/custom-directives) section.

</div>

#### Conjunction

Conjunction creates a single directive out of two directives: `directive1 & directive2`.

* `directive1` and `directive2` will be executed sequentially;
* if the first directive consumes parts of the path, the second one will "see" the remaining part;
* the combined directive will match only if **both** directives match;
* the outputs of the directives will be combined.

<div class="bg-blue-100 p-2 text-sm">

The outputs are combined into a tuple (or a scalar, or a unit – depending on the outputs of the combined directives)
**and** "flattened".

Assuming `A`, `B`, `C` and `D` are scalars (not tuples):

* `Directive[A]` & `Directive[B]` --> `Directive[(A, B)]`
* `Directive[(A, B)]` & `Directive[C]` --> `Directive[(A, B, C)]`
* `Directive[A]` & `Directive[(B, C)]` --> `Directive[(A, B, C)]`
* `Directive[(A, B)]` & `Directive[(C, D)]` --> `Directive[(A, B, C, D)]`
* etc.

Units are excluded:

For **any** `T` (scalar or tuple):

* `Directive[T]` & `Directive[Unit]` --> `Directive[T]`
* `Directive[Unit]` & `Directive[T]` --> `Directive[T]`
* `Directive[Unit]` & `Directive[Unit]` --> `Directive[Unit]`

</div>

When a directive provides a tuple of values, we can access those values by pattern-matching the tuple:

```scala
(path(segment) & param("page")) { case (theSegment, thePage) =>
  div(s"page $thePage at /$theSegment")
}
```

Similar to unit directives, it is very common to have directives providing tuples, and having to use the pattern
matching with `case` can get annoying, too. So instead, we can provide a multi-parameter function:

```scala
(path(segment) & param("page")) { (theSegment, thePage) =>
  div(s"page $thePage at /$theSegment")
}
```

Naturally, we can combine more than two directives with `&`:

```scala
(path(segment) & param("skip") & param("take")) { (theSegment, skip, take) =>
  div(s"skipping $skip and taking $take things at /$theSegment")
}
```

#### Disjunction

Disjunction allows us to try matching and/or extracting a value using two alternative
directives: `directive1 | directive2`.

* `directive1` and `directive2` will be executed in order, until one of them matches;
* the combined directive will match if **any** of the directives match;
* if both directives reject, the combined directive rejects as well;
* the output of the combined directive will be the output of the first directive to match;
* if `directive1` matches, `directive2` will not be executed.

```scala
(path("legacy-path") | path("new-path")) {
  div(s"It's either /legacy-path or /new-path")
}
```

#### Alternative routes

There are two ways of describing alternative routes. One is to have multiple sibling routes (we've seen this before):

```scala
div(
  pathEnd {
    div("Path is /")
  },
  path("my-path") {
    div(s"Path is /my-path")
  },
  noneMatched {
    div("Path is something else")
  }
)
```

In this case, the three routes are mutually exclusive:

* the path cannot be `/` and `/my-path` at the same time, so `pathEnd` and `path("my-path")` cannot both match;
* `noneMatched` is a special directive which matches **only** when none of the **previous** sibling routes had matched.

But it is possible to have sibling routes that can both match:

```scala
div(
  pathEnd {
    div("Path is /")
  },
  pathEnd {
    div(s"Path is /, again.")
  }
)
```

Another way to describe alternative routes is the `concat(routes: Route*): Route` function:

```scala
div(
  concat(
    pathEnd {
      div("Path is /")
    },
    path("my-path") {
      div(s"Path is /my-path")
    },
    div("Path is something else")
  )
)
```

When using `concat`, at most **one** of the routes will match (the rest will not be evaluated). Another difference is
that, similar to "nesting" directives, only one combined route will be applied as a modifier to the element, with just
one subscription to the location signal to drive it.

Both approaches are valid – we can use whichever we like more.