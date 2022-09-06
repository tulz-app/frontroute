# `firstMatch`

`firstMatch` takes a list of routes and returns a new route which tries to apply the alternatives in the order
they are provided, until one of them matches. Rejects if all the alternatives reject.

```scala
firstMatch(
  pathEnd { /*...*/ },
  pathPrefix("pages") {
    firstMatch(
      path("page-1") { /*...*/ },
      path("page-2") { /*...*/ }
    )
  }
)
```
