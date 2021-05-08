# `concat`

`concat` takes a list of alternative routes and returns a new route (lets call it `concat-route`):

* if any of the alternative routes completes (or order) – `concat-route` will complete with the same result
* if all the alternative routes reject – `concat-route` will reject as well

```scala
concat(
  pathEnd { /*...*/ },
  pathPrefix("pages") {
    concat(
      path("page-1") { /*...*/ },
      path("page-2") { /*...*/ }
    )
  }
)
```
