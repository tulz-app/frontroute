## Handling 'Not Found'

In the [First routes](first-routes) section we had the following:

```scala
div(

  path("blog") {
    div("Blog")
  },

  path("news") {
    div("News")
  }

)
```

What if we want to render a "not found" message when the path is neither `blog` nor `news`?

There are two ways to do it in `frontroute`: 
* using `firstMatch` with the "not found" element as the last alternative, or 
* using `noneMatched` directive.

### Important note

Do NOT use `firstMatch` and `noneMatched` together: this will not work. For example, in the following example,
the not-found route will *never* match:

```scala
div(
  firstMatch(
    path("blog") {
      div("Blog")
    },
    noneMatched(
      div("Not found")
    )
  )
)
```

### `noneMatched` directive

`noneMatched` is a special directive in `frontroute`: it doesn't check the current URL – rather, it checks
if any of the previous sibling routes had matched:

```scala
div(

  path("blog") {
    div("Blog")
  },

  path("news") {
    div("News")
  },

  noneMatched {
    div("Not found")
  }

)
```

The caveat with `noneMatched` is this: it depends on the order of the routes, and only knows 
about the sibling routes that come **before** it. Thus, in order for it to work correctly, it has to 
be the last route among the siblings.

For example, in the following example, both `<div>News</div>` and `<div>Not found</div>` will be rendered
if the path is `/news`:


```scala
div(

  path("blog") {
    div("Blog")
  },

  noneMatched { // will only check whether the path("blog") had matched, doesn't "know" about path("news")  
    div("Not found")
  },

  path("news") {
    div("News")
  }

)
```

#### Using `firstMatch`

We can achieve the same behaviour using the `firstMatch` function:

```scala
div(
  firstMatch(
    path("blog") {
      div("Blog")
    },
    path("news") {
      div("News")
    },
    div("Not found")
  )
)
```

Here we pass `div("Not found")` as the last alternative to the `firstMatch`. `div("Not found")` is converted into a route
with no directive – it will always match. Same as with `noneMatched`, the "not found" element
has to come last: in the following example, the `<div>News</div>` element will **never** be rendered:

```scala
div(
  firstMatch(
    path("blog") {
      div("Blog")
    },
    div("Not found"),
    path("news") {
      div("News")
    }    
  )
)
```

