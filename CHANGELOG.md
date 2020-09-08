# Changelog

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
