So far, we only had very simple examples. In this section we'll consider a few more complicated
use-cases and see how we can implement them using `frontroute`.

## Common prefix

Use case: 

* we have a set of pages which have a common prefix

```
/account
/account/settings
/account/subscription
/account/billing
```

The simplest way of describing the routes is to enumerate all pages as is:

```scala
div(
  path("account") {
    div("Account Home")
  },
  path("account" / "settings") {
    div("Settings")
  },
  path("account" / "subscription") {
    div("Subscription")
  },
  path("account" / "billing") {
    div("Billing")
  }
)
```

Since there is some repetition here (the `"account"` prefix), we can use the `pathPrefix` directive and nesting:

```scala
div(
  pathPrefix("account") {
    firstMatch(
      pathEnd {
        div("Account Home")
      },
      path("settings") {
        div("Settings")
      },
      path("subscription") {
        div("Subscription")
      },
      path("billing") {
        div("Billing")
      }      
    )
  }
)
```

<div class="bg-sky-200 px-8 py-2 text-sm">

`pathEnd` is a directive that matches whenever the `path` is empty (either because the URL is `/` or because 
all segments have been consumed by the previous directives).

`pathPrefix` is similar to the `path` directive, but also matches if the path has more segments.

</div>

In this particular case we had to introduce the `firstMatch`, which arguably makes the route look less
"pretty" or readable. But it depends on the situation you have and what you want to achieve.

## Query parameters

Use case:

* we have a listing page with pagination

```
/posts?page=(optional number, default 0)
```

```scala
import frontroute._

import scala.util.Try

def intParam(name: String): Directive[Int] =
  param(name).emap { string =>
    Try(string.toInt).toEither
  }

div(
  (path("posts") & intParam("page").opt.default(0)) { page => 
    div(s"Posts page: $page")
  }  
)
```

See [live example](/examples/advanced-query-parameters/live).

---

Here, we define a custom directive `intParam`: 

* first, we use the built-in `param` directive, which will extract the query parameter (it will reject
  if the parameter is missing)
* next, we use the `.emap` combinator – we try parsing the value as an `Int` and return an `Either`
* the directive created by `.emap` will reject when the returned `Either` is a `Left` (ignoring the left value).

Next, we use this directive: 
* `intParam("page")` – will reject if there is no "page" parameter or it cannot be parsed as an `Int`,
* make it optional (`.opt`) – will never reject, but provides an `Option[Int]`,
* and finally, we provide a default value (`.default(0)`).

This way, `intParam("page").opt.default(0)` is a `Directive[Int]`, which will never reject and will provide 
the `page` parameter parsed as `Int` (if present and parseable) or the default value of `0`.

<div class="bg-sky-200 px-8 py-2 text-sm">
To be continued...
</div>