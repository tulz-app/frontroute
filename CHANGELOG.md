# Changelog

### 0.14.0

* Internal implementation refactoring.
* Scala.js 1.6.0+
* API breaking: `pathMatcher` combinators (`.map`, `.filter`, etc) no longer accept a `description` parameter
* API new: `.recover`, `.emap` and `.tryParse` for `PathMatchers`
* API breaking: `fromTry` path matcher has been removed
* API new: new path matchers
  * `segment(oneOf: Seq[String])`
  * `segment(oneOf: Set[String])`
* API new: implicit conversion from `Set[String]` and `Seq[String]` to `PathMatcher[String]`
* API new: `testPath` and `testPathPrefix` directives (same as `path` and `pathPrefix` but preserving the unmatched path)
* API new: `memoize` directive ([example](https://frontroute.dev/examples/memoize))

### 0.13.3

* Breaking: one of the `complete` overloads is now `completeN`

The reason for the change is that its argument needs to be by-name (same as the other overload),
and the two overloads would end up having the same byte-code signature.

### 0.13.2

Sourcemaps are now pointing to GitHub.

### 0.13.1

* API: new implicit conversion from `Regex` to a path matcher

### 0.13.0

Update to Airstream `v0.13.0`. Publishing for Scala 3.0.0-RC3.

History and title behavior updates.

* API: `LinkHandler` now looks at the `data-title` attribute of anchors and uses it when pushing state
* API: `LinkHandler.install` now accepts a `defaultTitle: String = ""` argument to use when the `data-title` is missing
* Change: `LinkHandler` will not be pushing state when the current path, search query and hash are equal to the ones in the 
  anchor
* API: new `BrowserNavigation.replaceTitle(title: String)` function, that replaces the document title (and, optionally,
  the `<title>` element in the `<head>`) and stores the new title in the history state.

`LocationProvider.browser` now has three new parameters: 

* `setTitleOnPopStateEvents: Boolean = true` – if `true`, it will be updating the document title with the title saved by frontroute in 
  the PopState event state
* `updateTitleElement: Boolean = true` – if `true`, it will also be updating the content of the head/title element
* `ignoreEmptyTitle: Boolean = false` – if `true`, it will not be doing anything if the `title` is empty

### 0.12.2

Update to Airstream `v0.12.2`. No longer publishing for Scala 3.0.0-RC1.

* Bugfix: historyState directive was returning `undefined` due to emitted pop state events did not include the state value

### 0.12.0

Update to Airstream `v0.12.0`. Publishing for Scala 3.0.0-RC1

* Breaking: the `io.frontroute.directives._` no longer exists and importing it is not needed anymore
* API: renamed `RouteLocationProvider` into `LocationProvider`
* API: new `CustomLocationProvider`
* API: renamed `completeN` into `complete`
* API: new `state` directive
* API: new `makeRoute` and `makeRouteWithCallback`
* Util: new `LocationUtils` object with `parseLocationParams` and `encodeLocationParams` functions
* Bugfix: pop state event processing – event itself was used as `state`
* Bugfix: link handler was not verifying if the link was pointing to the URL within the same origin, caused errors 
  (changing the origin when pushing state is not allowed)

### 0.11.7

* Bugfix: `.collect` and `.mapTo` were not storing the mapped value (in the internal state)

An example that would show this behavior:

```scala
(pathEnd.mapTo(true) | path("page-1").mapTo(false)) { isIndex =>
  render(Page(isIndex))
}
```

### 0.11.6

* Bugfix: disjunction
* Bugfix: `.map` was not storing the mapped value (in the internal state)

The value of the disjunction was not stored (in the internal state), thus a disjunction would not
match multiple times in a row (even if the resulting values were different).

An example that would show this behavior:

```scala
(pathEnd.map(_ => true) | path("page-1").map(_ => false)) { isIndex =>
  render(Page(isIndex))
}
```

* API: new combinators for directives
    * `.some` – transforms a `Directive[A]` into a `Directive[Option[A]]` (with `Some(_)` value)
    * `.none` – transforms a `Directive[A]` into a `Directive[Option[A]]` (with `None` value)
    * `.mapOption` – transforms a `Directive[Option[A]]` into a `Directive[Option[B]]` (applying the provided function to the `Option`)

### 0.11.5

Fixing `LinkHandler`.

### 0.11.4

* New: LinkHandler
* New: directives: `origin`, `host`, `hostname`, `port` and `protocol`
* simplified directives and path matchers (single values are now scalars, not Tuple1), no more `Tuple` marker
* Breaking: Scala.js 1.3.1+ is now required

### 0.11.3

Update the tuplez dependency to `v0.3.0`, no other changes.

### 0.11.2

* preserving scroll position in history state
* moved apply converters into the `tuplez` library  
* New: `historyScroll` / `historyState` directives
* Breaking: `BrowserRouteLocationProvider` is removed, replaced by `BrowserNavigation.locationProvider`

### 0.11.1

No code changes. Fixed the publish settings to not exclude the sources from the sources artifact.

### 0.11.0

* "re-branding" as frontroute
* updated to Airstream v0.11.1

### 0.10.1

* Bugfix: disjunction + signal
  ```scala
  // this will work now
  (path(segment) | pathEnd.tmap(_ => Tuple1("default")).signal { 
  // ...
  }
  ```
* API: laminar-route does not depend on Laminar anymore (like Waypoint)  
  * Add `$popStateEvent` to `BrowserRouteLocationProvider` constructor
  * Migration: You should provide `$popStateEvent = windowEvents.onPopState` if using Laminar
* API: new utility object - `BrowserNavigation` with `pushState` function

### 0.10.0

* initial public release
