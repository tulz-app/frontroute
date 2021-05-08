# .signal directive

* `.signal: Directive[Signal[L]]`

Transforms a `Directive[L]` into a `Directive[Signal[L]]`.

Whenever you have a directive, you can call the `.signal` function on it.

```scala
param("param-name").signal { (paramSignal: Signal[String]) =>
  /* ... */
}
```

## When would this be useful?

Consider the following example:

```scala
pathPrefix("dashboard") {
  maybeParam("tab").map(_.getOrElse("summary")) { selectedTab => // : String 
    render(Page.Dashboard(selectedTab))   
  }
}
```

In this case, whenever the query string changes to have a different value for the `tab` parameter, the route will 
be re-evaluated and `render(Page.Dashboard(selectedTab))` will be called again. Depending on the way you implement 
your "actions" inside the `complete`s, this might not be what you want.

For example, you might be re-rendering (to keep things simple) the whole page from scratch whenever `render` 
is called with a new `Page` value.

But in this case you might want to keep the rendered page and all the DOM and state, but change a visibility of 
some elements on the page according to the `tab` parameter.

Now, if you use the `.signal` combinator:

```scala
pathPrefix("dashboard") {
  maybeParam("tab").map(_.getOrElse("summary")).signal { (selectedTab: Signal[String]) => 
    render(Page.Dashboard(selectedTab))   
  }
}
```

the `selectedTab` will become a `Signal[String]`, and when the `tab` parameter changes, `render` will not be called again — but rather the value inside the
`selectedTab` signal will change, and you can react to it in your rendering logic.
