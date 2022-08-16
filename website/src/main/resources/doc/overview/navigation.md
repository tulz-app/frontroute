# Navigation

`BrowserNavigation` object contains a set of utility methods for programmatic navigation.



### pushState / replaceState

```scala
def pushState(
  data: js.Any = js.undefined,
  title: String = "",
  url: js.UndefOr[String] = js.undefined,
  popStateEvent: Boolean = true
): Unit

def replaceState(
  url: js.UndefOr[String] = js.undefined,
  title: String = "",
  data: js.Any = js.undefined,
  popStateEvent: Boolean = true
): Unit
```

These methods should be used for navigation instead of directly calling `window.history.pushState` / `window.history.replaceState`.

If `popStateEvent` is `true`, `emitPopStateEvent` will be invoked as well (the browser does not emit this event in case of
programmatic history push/replace).



### emitPopStateEvent

```scala
def emitPopStateEvent(): Unit
```

Emits (`dom.window.dispatchEvent`) a `popstate` event.

You will most likely need to call this after your app with routes is mounted.



### preserveScroll

```scala
def preserveScroll(keep: Boolean): Unit
```

Configures whether `BrowserNavigation` should preserve the window scroll location (in the history state) when pushing state (`pushState`).



## restoreScroll

```scala
def restoreScroll(): Unit
```

If scroll position is available in the current history state — scrolls the window to that position. You might want to use this after you render your content and want the `back`/`forward` buttons
to get the user to the position on the page where they used to be before navigation.
