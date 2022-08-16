# `concat`

`concat` takes a list of alternative routes and returns a new route which tries to apply the alternatives in the order
they are provided. 

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
