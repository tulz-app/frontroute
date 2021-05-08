# Injecting an external signal into the routing

* `signal[T](signal: Signal[T]): Directive[T]` — provide an external `Signal[T]` and get a `Directive[T]`

Whenever the underlying signal changes, the route will be re-evaluated.

This can be useful for integrating with third-party libs, or, for example, to integrate you authentication mechanism with the routes:


```scala
val currentUser: Signal[Option[User]] = ???

def authenticatedUser: Directive[User] =
  signal(currentUser).flatMap {
    case Some(a) => provide(a)
    case _       => reject
  }
  
val route =
  concat(
    pathPrefix("public") {
      ...
    },
    (pathPrefix("private") & authenticatedUser) { user => 
      ...
    }  
  )

```
