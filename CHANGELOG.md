# Changelog

### 0.11.6

Disjunction bug fix.

The value of the disjunction was not preserved, thus a disjunction would not 
match multiple times in a row (even if the provided value was different).


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
