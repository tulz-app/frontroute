# Changelog

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
