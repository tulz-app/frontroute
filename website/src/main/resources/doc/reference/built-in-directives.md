### reject

```scala
val reject: Route
```

Returns a `Route` that always rejects.



### complete

```
def complete(result: => ToComplete): Route
```

Returns a `Route` that always matches and renders the element provided by the `result: => ToComplete`.

You can pass an `Element` or a `Signal[Element]` to `complete`: implicit conversions from those to `ToComplete`
are provided out of the box:

```scala
implicit def elementToComplete(value: Element): ToComplete
implicit def signalToComplete(value: Signal[Element]): ToComplete
```

```scala
complete(div())
complete(Val(div()))
```



### provide

```scala
def provide[L](value: L): Directive[L]
```

Always matches. Provides the given `value`.



### noneMatched

```scala
val noneMatched: Directive0
```

Matches if none of the previous sibling routes has matched.

```scala
div(
  pathEnd { div("end") },
  noneMatched { div("not found")} 
)
```



### debug

```scala
def debug(message: Any, optionalParams: Any*)(subRoute: Route): Route
```

Always matches. Prints a debug message (using `dom.console.debug`) before invoking the `subRoute`.



### pathEnd

```scala
val pathEnd: Directive0
```

Matches if the unmatched path is empty.



### pathPrefix

```scala
def pathPrefix[T](m: PathMatcher[T]): Directive[T]
```

Matches if the given path matcher matches the unmatched path. Provides the value provided by the path matcher. Removes
the parts matched by the path matcher from unmatched path for the nested routes.

```scala
pathPrefix("user")
// directive matches if the "unmatched path" starts with a "user" segment
// this is a Directive0 as the constant string path matcher doesn't provide a value

pathPrefix("user" / segment)
// matches if the "unmatched path" starts with a "user" segment followed by another segment
// this is a Directive[String] because the 'segment' path matcher is a PathMatcher[String] 
```



### path

```scala
def path[T](m: PathMatcher[T]): Directive[T]
```

Effectively the same as `pathPrefix(m) & pathEnd`



### testPathPrefix

```scala
def testPathPrefix[T](m: PathMatcher[T]): Directive[T]
```

Same as `pathPrefix` but does not consume the unmatched path.



### testPath

```scala
def testPath[T](m: PathMatcher[T]): Directive[T]
```

Same as `path` but does not consume the unmatched path.



### extractUnmatchedPath

```scala
val extractUnmatchedPath: Directive[List[String]]
```

Always matches. Provides the unmatched path without "consuming" (see [Path-matching](/overview/path-matcher)) it.



### param

```scala
def param(name: String): Directive[String]
```

Matches if the search query contains the given parameter. Provides the value of the parameter in the search query.



### maybeParam

```scala
def maybeParam(name: String): Directive[Option[String]]
```

Always matches. If the search query contains the given parameter, provides its value in a `Some()`. Otherwise,
provides `None`.



### state

```scala
def state[T](initial: => T): Directive[T]
```

Always matches. The first time this directive is hit, creates the state value provided by `initial` and memoizes it.
Provides the memoized value.



### signal

```scala
def signal[T](signal: Signal[T]): Directive[T]
```

Always matches. Provides the value inside the signal. Whenever the signal value changes, forces the route to be
re-evaluated.

See [example]({{sitePrefix}}/examples/signal).



### historyState

```scala
def historyState: Directive[Option[js.Any]]
```

Always matches. Provides the history state if it was set by `pushState` or `replaceState`. Otherwise, provides `None`.

Extracts the history state (will only work if [BrowserNavigation]({{sitePrefix}}/overview/navigation) is used for `pushState`
/`replaceState`).



### historyScroll

```scala
def historyScroll: Directive[Option[ScrollPosition]]
```

Always matches. Provides the window scroll position if it was stored in the state. Otherwise, provides `None`.

When [BrowserNavigation]({{sitePrefix}}/overview/navigation) is used for `pushState`/`replaceState`, it can preserve the
window scroll position when navigating (enabled by default). 

This directive returns the preserved window scroll position (if any).



### extractHostname

```scala
val extractHostname: Directive[String]
```

Always matches. Provides the `hostname` part of the location (`window.location.hostname`).



### extractPort

```scala
val extractPort: Directive[String]
```

Always matches. Provides the `port` part of the location (`window.location.port`).



### extractHost

```scala
val extractHost: Directive[String]
```

Always matches. Provides the `host` part of the location (`window.location.host` – `hostname:port`)).



### extractProtocol

```scala
val extractProtocol: Directive[String]
```

Always matches. Provides the `protocol` part of the location (`window.location.protocol`).



### extractOrigin

```scala
val extractOrigin: Directive[Option[String]]
```

Always matches. Provides the `origin` part of the location (`window.location.origin`).
