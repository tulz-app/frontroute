# Injecting an external signal into the routing

* `signal[T](signal: Signal[T]): Directive[T]` — provide an external `Signal[T]` and get a `Directive[T]`

Whenever the underlying signal changes, the route will be re-evaluated.

This can be useful for integrating with third-party libs, or, for example, to integrate your authentication mechanism
into the routing:

```scala
val $currentUser: Signal[Option[User]] = ???

def authenticatedUser(implicit maybeUser: Option[User]) =
  maybeUser match {
    case Some(user) => provide(user)
    case _ => reject
  }

val route =
  signal($currentUser) { implicit maybeUser =>
    concat(
      pathPrefix("public") {
        ???
      },
      (pathPrefix("private") & authenticatedUser) { user =>
        ???
      }
    )
  }

```
