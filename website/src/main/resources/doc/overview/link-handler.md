# Link handler

You can call `LinkHandler.install()` at the app start:

```scala
import io.frontroute._

LinkHandler.install()
```

It registers a click handler for all `<a>` elements on the page (existing and future):
* when `rel` is empty or not set, and if the target origin is the same – calls `BrowserNavigation.pushState` with the anchor's `href`
* when `rel` is `external` – opens the anchor's `href` in a new tab (`dom.window.open(anchor.href)`)
* when `res` has a different non-empty value – the click event is propagated.

It also registers a global `window.routeTo` function with one parameter – `path`, which calls `BrowserNavigation.pushState(path = path)`.
