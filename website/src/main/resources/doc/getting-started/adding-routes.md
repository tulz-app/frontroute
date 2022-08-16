### Simple routes

Let's add a few routes to our app:

```scala
val myApp: Element = 
  div(
    pathEnd {
      div("Root page")
    },
    path("tag-page" / segment) { theTag =>
      div(s"Tag page, tag: $theTag")
    },
    noneMatched {
      div("Not found")
    }
  )
```
