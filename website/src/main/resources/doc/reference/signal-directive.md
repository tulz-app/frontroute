# .signal

```scala
(d: Directive[L]).signal: Directive[Signal[L]]
```

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
def MyPage(selectedTab: String): Element = ???

pathPrefix("dashboard") {
  maybeParam("tab").map(_.getOrElse("summary")) { (selectedTab: String) =>  
    MyPage(selectedTab)   
  }
}
```

In this case, whenever the query string changes to have a different value for the `tab` parameter, the route will 
be re-evaluated and `MyPage(selectedTab)` will be called again.

For example, you might not want to re-render the whole page from scratch whenever `selectedTab` 
changes. Rather, you might want to keep the rendered page with all the DOM and state, and only change a visibility of 
some elements on the page according to the `selectedTab` parameter.

To achieve that, you can use the `.signal` combinator:

```scala
def MyPage(selectedTab: Signal[String]): Element = ???

pathPrefix("dashboard") {
  maybeParam("tab").map(_.getOrElse("summary")).signal { (selectedTab: Signal[String]) =>
    MyPage(selectedTab)   
  }
}
```

The `selectedTab` is now a `Signal[String]` (not a `String`), and when the `tab` parameter changes, 
`MyPage` will not be called again — rather, the value inside the
`selectedTab` signal will change, and you can react to it in your rendering logic.
