
<div class="bg-sky-200 px-8 py-2 text-sm">

Under the hood, a route is defined by a function, with a signature that approximately looks like
this: `Signal[Location] => Option[Element]`
(the actual signature is a bit more complicated, but that is an implementation detail, and we will not be dealing with
it directly). `Location` is an equivalent of the
[Window.location](https://developer.mozilla.org/en-US/docs/Web/API/Window/location). Because we're in a single page
application, and `Window.location` can change dynamically, the "current location" is modeled as a `Signal[Location]`.

</div>

---

<div class="bg-sky-200 px-8 py-2 text-sm">

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


