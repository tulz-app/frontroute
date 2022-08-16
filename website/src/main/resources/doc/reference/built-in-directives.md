### `pathEnd`

```scala
val pathEnd: Directive0
```

Matches if the current path is empty.



### `pathPrefix`

```scala
def pathPrefix[T](m: PathMatcher[T]): Directive[T]
```

Applies the given `PathMatcher` to the current path. 

If it matches: 
* the directive matches as well, with the value provided by the path matcher;
* the parts matched by the path matcher are removed from unmatched path (for the subsequent directives and routes).

If the path matcher rejects, the directive rejects as well.

```scala
pathPrefix("user")
```

This `pathPrefix` matches if the current path starts with a `"user"` segment.

It's a `Directive0` because a string constant (`"user"` in this case) is implicitly converted into 
a `PathMatcher[Unit]`.

```scala
pathPrefix("user" / segment)
```

This `pathPrefix` matches if the current path starts with a `"user"` segment followed by at least one more segment.

It's a `Directive[String]` because `segment` is a `PathMatcher[String]`.


See [Path matching]({{sitePrefix}}/overview/path-matcher) for more details.

### `path`

```scala
def path[T](m: PathMatcher[T]): Directive[T]
```

Effectively the same as `pathPrefix(m) & pathEnd`



### `testPathPrefix`

```scala
def testPathPrefix[T](m: PathMatcher[T]): Directive[T]
```

Same as `pathPrefix` but does not consume the segments of the unmatched path.



### `testPath`

```scala
def testPath[T](m: PathMatcher[T]): Directive[T]
```

Same as `path` but does not consume the segments of the unmatched path.



### `extractUnmatchedPath`

```scala
val extractUnmatchedPath: Directive[List[String]]
```

Always matches. Provides the current path without "consuming" it.



### `param`

```scala
def param(name: String): Directive[String]
```

Matches if the search query contains the given parameter. Provides the value of the parameter in the search query.



### `maybeParam`

```scala
def maybeParam(name: String): Directive[Option[String]]
```

Always matches. If the search query contains the given parameter, provides its value in a `Some()`. Otherwise,
provides `None`.



### `signal`

```scala
def signal[T](signal: Signal[T]): Directive[T]
```

Always matches. Provides the value inside the signal. Whenever the signal value changes, forces the route to be
re-evaluated.

See [example]({{sitePrefix}}/examples/signal).


### `provide`

```scala
def provide[L](value: L): Directive[L]
```

Always matches with the given `value`.



### `noneMatched`

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



### `debug`

```scala
def debug(message: Any, optionalParams: Any*)(subRoute: Route): Route
```

Always matches. Prints a debug message (using `dom.console.debug`) before invoking the `subRoute`.






### `state`

```scala
def state[T](initial: => T): Directive[T]
```

Always matches. The first time this directive is hit, creates the state value provided by `initial` and memoizes it.
Provides the memoized value.






### `historyState`

```scala
def historyState: Directive[Option[js.Any]]
```

Always matches. Provides the history state if it was set by `pushState` or `replaceState`. Otherwise, provides `None`.

Extracts the history state (this will only work if [BrowserNavigation]({{sitePrefix}}/overview/navigation) is used for `pushState`
/`replaceState`, not direct calls to the History API).



### `historyScroll`

```scala
def historyScroll: Directive[Option[ScrollPosition]]
```

Always matches. Provides the window scroll position if it was stored in the state. Otherwise, provides `None`.

When [BrowserNavigation]({{sitePrefix}}/overview/navigation) is used for `pushState`/`replaceState`, it can preserve the
window scroll position when navigating (enabled by default). 

This directive returns the preserved window scroll position (if any).



### `extractHostname`

```scala
val extractHostname: Directive[String]
```

Always matches. Provides the `hostname` part of the location (`window.location.hostname`).



### `extractPort`

```scala
val extractPort: Directive[String]
```

Always matches. Provides the `port` part of the location (`window.location.port`).



### `extractHost`

```scala
val extractHost: Directive[String]
```

Always matches. Provides the `host` part of the location (`window.location.host` – `hostname:port`)).



### `extractProtocol`

```scala
val extractProtocol: Directive[String]
```

Always matches. Provides the `protocol` part of the location (`window.location.protocol`).



### `extractOrigin`

```scala
val extractOrigin: Directive[Option[String]]
```

Always matches. Provides the `origin` part of the location (`window.location.origin`).


