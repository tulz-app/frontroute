## First routes

First, let's make a simple app with a couple of routes to see what routing with `frontroute` looks like.

Say we want the following:

* when the location is `/blog`, it should render `<div>Blog</div>`,
* when the location is `/news`, it should render `<div>News</div>`.

```scala
import com.raquo.laminar.api.L.*
import io.frontroute.*

val myApp = div(

  path("blog") {
    div("Blog")
  },

  path("news") {
    div("News")
  }

)
```

And that is all it takes to have routing in our app. It looks like a regular Laminar app, except we added two new
constructs:

* `path("blog") { ... }`, and
* `path("news") { ... }`.

These are the `Route`-s.

A route is a regular Laminar modifier which acts similarly to the `child.maybe <-- ...` modifier:

* when the route matches, the corresponding element is inserted into the parent,
* otherwise, the element is removed.

Thus, all we need to do is put the routes inside our view tree. When we mount `myApp`, one of the two nested `div()`
elements will be rendered – depending on the current path in the browser. If the path is neither `/news` nor `/blog` –
none of the routes will match and none of the nested `div()` elements will be rendered.

### Routing basics

The task of the router is to render elements on the page according to the
URL ([window.location](https://developer.mozilla.org/en-US/docs/Web/API/Window/location)).

<div class="bg-sky-200 px-8 py-2 text-sm">

In `frontroute`, `window.location` is parsed into a `Location` case class:

```scala
case class Location(
  path: List[String],
  // ...
)
```

`path` is a parsed list of segments in the `window.location.pathname`:

* `/` -> `List.empty`,
* `/blog/posts/42` -> `List("blog", "posts", "42")`, etc.

Because we're in a single page application, and `window.location` can change dynamically, location is modeled as
a `Signal[Location]`.

</div>

In order for `frontroute` to know which elements to render, we need to describe the "routing rules". We do this
with `Route`-s. A route comprises a "routing rule" and a corresponding element to be rendered when the rule matches.

In `frontroute`, "routing rules" are defined using `Directives`. In the example above we've used the `path` directive, which
checks if the path consists of a single segment (`/blog` or `/news`):

* `path("blog")`, and
* `path("news")`.



<div class="bg-sky-200 px-8 py-2 text-sm">

`frontroute` routes and directives are inspired
by [Akka HTTP routing DSL](https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/index.html).

</div>

### Directives

Directives alone are not routes, nor they are modifiers, and cannot be applied to elements. The following will not
compile (nor does it have any meaning):

```scala
div(
  path("news")
)
```

The role of a directive is to do the actual matching against the current URL. While matching, directives can also
extract information from the location and provide it to the rendered element.

Thus, in order to get a `Route`, we start with a directive and provide an `Element` to be rendered when the directive
matches. This is done by calling the `.apply` method on the directive. With `.apply` spelled out explicitly,
the `path("blog") { ... }`
route from the above example would look like this:

```scala
path("blog").apply {
  div("Blog")
}
```

Directives have a type parameter which describes the value that the directive will extract.

The directives we've seen so far happen to not extract anything and have type `Directive[Unit]` (aliased
as `Directive0`). The syntax for such directives might have looked like the following:

```scala
path("blog") { (_: Unit) =>
  div("Blog")
}
```

But because these directives are so common, and having to put `_ => ` can get annoying quickly (and it doesn't look very
nice), we have a special syntax for `Directive0`-s, which allows us to pass an element by-name (like we were doing in
the examples above):

```scala
path("blog") {
  div("Path is /") // passed by-name, will not be evaluated until it's needed  
}
```

For directives that **do** extract a non-unit value, instead of a by-name element, we provide a function that accepts
the output of the directive and returns an element:

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

<div class="bg-sky-200 px-8 py-2 text-sm">

We have seen the `path` directive already, in the first example: `path("blog")` and `path("news")`.

There, it was a `Directive[Unit]`, while in this case, `path(segment)` is a `Directive[String]`.

This is because the output the `path` directive is defined by the "path matcher" that we use:

* a string `"blog"` is implicitly converted into a `PathMatcher[Unit]`, which checks if the current `path` contains a
  single segment equal to `"blog"`, and does not extract anything; this can be done explicitly:
  ```scala
  path(segment("blog"))
  ```
* `segment` (without parameters) is built-in path matcher with type `PathMatcher[String]`, which checks if the
  current `path` contains a single segment
  (without checking its value), and extracts that segment as the output.

Thus, `path("blog")` is a `Directive[Unit]`, and `path(segment)` is a `Directive[String]`.

</div>

#### Nesting directives

The description above is not 100% accurate, though.

We do not pass an `Element` directly to the `.apply` method of a directive: we pass a `Route`.

`Element`-s are implicitly converted into `Routes` to enable the simplified syntax (we don't have to use – or have –
the `complete`
function, like in Akka HTTP or in earlier versions of `frontroute`).

This enables us to "nest" the directives:

```scala
pathPrefix("posts") {
  path(segment) { postId =>
    div("Post ID: $postId") // div() is implicitly converted into a Route
  }
}
```

Here, the `pathPrefix("posts")` directive will be executed first. When it matches, it will run the inner route
– `path(segment) { ... }`. The inner route will "see" the location with the "posts" segment removed from the path – it
will have been "consumed" by the `pathPrefix("posts")` directive. If the `path(segment)` directive matches as well, the
whole route will match and `div("Post ID: $postId")` will be
rendered ([more about path matching](/overview/path-matcher)).

#### Combining directives

Nesting is one way of combining directives. We also have conjunction (`directive1 & directive2`) and
disjunction (`directive1 | directive2`).

<div class="bg-sky-200 px-8 py-2 text-sm">

Directives also have `.map` and `.flatMap`. This is covered in the [custom directives](/overview/custom-directives)
section.

</div>

#### Conjunction

Conjunction creates a single directive out of two directives: `directive1 & directive2`.

* `directive1` and `directive2` will be executed sequentially;
* if the first directive consumes parts of the path, the second one will "see" the remaining part;
* the combined directive will match only if **both** directives match;
* the outputs of the directives will be combined.

<div class="bg-sky-200 px-8 py-2 text-sm">

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
  path("news") {
    div("News")
  },
  path("blog") {
    div(s"Blog")
  }
)
```

In this case, the three routes are mutually exclusive: the path cannot be `/news` and `/blog` at the same time, so
the `path("news")` and `path("blog")` directives cannot both match;

But it is possible to have sibling routes that can both match:

```scala
div(
  path("news") {
    div("News")
  },
  path("news") {
    div("Also news")
  }
)
```

Here, if the path is `/news`, both nested `div()` elements will be rendered.

#### firstMatch

Another way to describe alternative routes is the `firstMatch(routes: Route*): Route` function:

```scala
div(
  firstMatch(
    path("news") {
      div("News")
    },
    path("blog") {
      div(s"Blog")
    },
    path("something-else") {
      div(s"Something else")
    }
  )
)
```

When using `firstMatch`, at most **one** of the routes will match (the rest will not be evaluated). Another difference
is that, similar to "nesting" directives, only one combined route will be applied as a modifier to the element, with a
single subscription to the location signal to drive it.

Both approaches are valid – we can use whichever we like more.
