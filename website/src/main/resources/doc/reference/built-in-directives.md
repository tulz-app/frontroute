###  `reject: Route`

Always rejects.

###  `provide[L](value: L): Directive[L]`

Always matches.
Provides the given value.

###  `complete[T](events: EventStream[() => Unit]): Route`

Completes the route with the given stream of actions.

###  `complete[T](action: => Unit): Route`

Completes the route with a stream emitting the given action.

###  `debug(message: Any, optionalParams: Any*)(subRoute: Route): Route`

Always matches.
Prints a debug message (using the `Logging` utility) before invoking `subRoute`.

See also: [Debugging](/overview/debugging).

###  `pathEnd: Directive0`

Matches if the unmatched path is empty.

###  `pathPrefix[T](m: PathMatcher[T]): Directive[T]`

Matches if the given path matcher matches the unmatched path.
Provides the value provided by the path matcher.
Removes the parts matched by the path matcher from unmatched path for the nested route.

```scala
pathPrefix("user") 
// directive matches if the "unmatched path" starts with a "user" segment
// this is a Directive0 as the constant string path matcher doesn't provide a value

pathPrefix("user" / segment)
// matches if the "unmatched path" starts with a "user" segment followed by another segment
// this is a Directive[String] because the 'segment' path matcher is a PathMatcher[String] 
```

###  `path[T](m: PathMatcher[T]): Directive[T]`

Effectively the same as `pathPrefix(m) & pathEnd`

###  `param(name: String): Directive[String]`

Matches if the search query contains the given parameter.
Provides the value of the parameter in the search query.

###  `maybeParam(name: String): Directive[Option[String]]`

Always matches.
If the search query contains the given parameter, provides its value in a `Some()`. Otherwise, provides `None`.

###  `state[T](initial: => T): Directive[T]`

Always matches.
The first time this directive is hit, creates the state value provided by `initial` and memoizes it.
Provides the memoized value.

###  `signal[T](signal: Signal[T]): Directive[T]`

Always matches.
Provides the value inside the signal. Whenever the signal value changes, forces the route to be re-evaluated.

###  `historyState: Directive[Option[js.Any]]`

Always matches.
Provides the history state if it was set by `pushState` or `replaceState`. Otherwise, provides `None`.

Extracts the history state (will only work if [BrowserNavigation](/overview/navigation) is used for `pushState`/`replaceState`).

###  `historyScroll: Directive[Option[ScrollPosition]]`

Always matches.
Provides the window scroll position if it was stored in the state. Otherwise, provides `None`.

When [BrowserNavigation](/overview/navigation) is used for `pushState`/`replaceState`, it can preserve the window scroll position when navigating
(enabled by default). This directive returns the preserved window scroll position (if any).

###  `extractUnmatchedPath: Directive[List[String]]`

Always matches.
Provides the unmatched path without "consuming" (see [Path-matching](/overview/path-matcher)) it.

###  `extractHostname: Directive[String]`

Always matches.
Provides the `hostname` part of the location (`window.location.hostname`).

###  `extractPort: Directive[String]`

Always matches.
Provides the `port` part of the location (`window.location.port`).

###  `extractHost: Directive[String]`

Always matches.
Provides the `host` part of the location (`window.location.host` – `hostname:port`)).

###  `extractProtocol: Directive[String]`

Always matches.
Provides the `protocol` part of the location (`window.location.protocol`).

###  `extractOrigin: Directive[String]`

Always matches.
Provides the `origin` part of the location (`window.location.origin`).
