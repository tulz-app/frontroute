# Changelog

### 0.12.0 \[M1\]

Update to Airstream `v0.12.0-M1`.

* API: renamed `RouteLocationProvider` into `LocationProvider`
* API: new `CustomLocationProvider`
* API: renamed `completeN` into `complete`
* API: new `state` directive

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
