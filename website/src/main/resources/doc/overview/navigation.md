# Navigation (History API)

`frontroute` uses (and depends on, in order to work correctly) the [History API](https://developer.mozilla.org/en-US/docs/Web/API/History).

`BrowserNavigation` object is provided and should be used for all navigation.

## BrowserNavigation.locationProvider

```scala
def locationProvider(popStateEvents: EventStream[dom.PopStateEvent]): LocationProvider
```

Creates an instance of `LocationProvider` that is required to run the routes (see description above).
It takes a single parameter — a stream of `PopStateEvent`. If you're using Laminar, this stream is provided by `windowEvents.onPopState`.

## BrowserNavigation.preserveScroll

```scala
def preserveScroll(keep: Boolean): Unit
```

Configures whether `BrowserNavigation` should preserve the window scroll location (in history state) when pushing state (`pushState`).

## emitPopStateEvent

```scala
def emitPopStateEvent(): Unit
```

Emits (`dom.window.dispatchEvent`) a `popstate` event. You will most likely need to call this right after calling `runRoute`.

## restoreScroll

```scala
def restoreScroll(): Unit
```

If scroll position is available in the current history state — scrolls the window to that position. You might want to use this after you render your content and want the `back`/`forward` buttons
to get the user to the position on the page where they used to be before navigation.

## pushState / replaceState

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

These functions should be used for navigation instead of directly calling `window.history.pushState` / `window.history.replaceState`.

If `popStateEvent` is `true`, `emitPopStateEvent` will be called right after `window.history.pushState` / `window.history.replaceState`
(the browser does not emit this event in case of programmatic history push/replace, set it to `false` only if you know what you are doing).
