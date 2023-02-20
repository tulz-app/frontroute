## Links and navigation

In a single page application, navigation has to be performed using [History.pushState](https://developer.mozilla.org/en-US/docs/Web/API/History/pushState) or
[History.replaceState](https://developer.mozilla.org/en-US/docs/Web/API/History/replaceState) in order to prevent page reloads.

For example, the following app will work, but the page will be reloaded every time the user 
clicks one of the links:

```scala
div(

  path("blog") {
    div("Blog")
  },

  path("news") {
    div("News")
  },

  div(
    a(href := "/blog", "Blog"),
    a(href := "/news", "News"),
  )
  
)
```

`frontroute` provides two ways of dealing with navigation:

* `LinkHandler`
* `BrowserNavigation`

### `LinkHandler`

`LinkHandler` is a utility provided by `frontroute` that makes `<a>` elements call `pushState` / `replaceState` instead of 
triggering the standard browser navigation.

In order to activate the link handler, you need to bind it at the top of your view tree:

```scala
import com.raquo.laminar.api.L._
import frontroute._

object Main {

  val myApp = div("My App")
  
  def main(args: Array[String]): Unit = {
    val appContainer = org.scalajs.dom.document.querySelector("#app")
    render(
      appContainer,
      myApp.amend(LinkHandler.bind)
    )
  }

}
```

When `LinkHandler` is active, it will add a listener to the `onClick` events of all `<a>` elements inside the element it is
bound to (including those that will be added to the DOM subsequently).

The `onClick` handler will do the following:
* if the origin of the link is the same as the current origin AND the `rel` of the `<a>` element 
  is either not defined, set to `"replace"`, or set to `""`:
  * if the path, query and hash of the link match the current path, query and hash – the click will be ignored;
  * otherwise, if the `rel` of the `<a>` element is `"replace"` – the `BrowserNavigation.replaceState` will be called
  * otherwise, the `BrowserNavigation.pushState` will be called
* if the `rel` of the `<a>` element is set to `"external"` – the link will be opened in a new tab; 
* otherwise, the default browser navigation will happen.


### `BrowserNavigation`

`BrowserNavigation` provides utilities to programmatically control the navigation:

* `BrowserNavigation.pushState`, and
* `BrowserNavigation.replaceState`

```scala
  import com.raquo.laminar.api.L._
  import frontroute._

  div(
    button(
      href := "/news", 
      onClick.preventDefault --> { _ => BrowserNavigation.pushState(url = "/blog") } ),
      "Blog"
    ,
    button(
      href := "/blog",
      onClick.preventDefault --> { _ => BrowserNavigation.pushState(url = "/news") },
      "News"
    )
  )
```

Sometimes it is what you need, but in most of the cases it is a repetitive boilerplate. 

### Relative links

(this section might be incomplete)

Relative links in HTML can sometimes be annoying (and sometimes it is not possible to achieve what we need).

Say we have these pages:

* `/cars/$id` (car summary)
* `/cars/$id/details` (car details)
* `/cars/$id/reviews` (car reviews)

And we want to define three `<a>` elements to point to those pages, on each of those pages:

```html
<a href="???">summary</a>
<a href="details">details</a>
```

We have two problems here.

We cannot make a (relative) link to `/cars/$id` that will work on both `/cars/$id/details` and `/cars/$id` pages: 
if we use `href=".."`, it will behave differently on different pages:
* it will (correctly) point to `/cars` on the `/cars/$id` page,
* but will point to `/cars/$id` on the `/cars/$id/details` page.

Likewise, we cannot make a (relative) link to `/cars/$id/details` either:
if we use `href="details"`:
* it will (correctly) point to `/cars/$id/details` on the `/cars/$id/details` page (or any other `/cars/$id/something` page),
* but will point to `/cars/details` on the `/cars/$id` page.

One solution to this is to use the absolute links. This works, but the component will need to know the exact path that led to
its rendering.

In `frontroute`, we have a special modifier: `relativeHref`. This modifier is aware of the current routing state, and will 
create absolute href-s from a relative href provided to it:


```scala
def CarComponent(carId: String) =
  div(
    
    div(
      // the following links will depend on the current "consumed" path in the routing,
      // in this particular example, the current "consumed" path will be /cars/$carId
      a(
        relativeHref(""), // will always point to /cars/$carId
        "Summary"
      ),
      a(
        relativeHref("details"), // will always point to /cars/$carId/details
        "Details"
      ),
      a(
        relativeHref("reviews"), // will always point to /cars/$carId/reviews
        "Reviews"
      )
      
    ),
    
    pathEnd {
      div("Summary")
    },
    path("details") {
      div("Details")
    },
    path("reviews") {
      div("Reviews")
    }
  )

div(
  path("cars" / segment) { carId => // this is where the path gets "consumed" before reaching the links
    CarComponent(carId)
  }
)
```

(see [example](/examples/matched-path/live))

### Redirects / rewrites

Sometimes we want to "redirect" the browser from one path to another:

* `/cars/$id` - should redirect to `.../summary`
* `/cars/$id/legacy-summary` - should also redirect to `.../summary`
* `/cars/$id/summary`
* `/cars/$id/details`

For this, we have the `navigate` function:


```scala
div(
  path("cars" / segment) { carId =>
    firstMatch(
      (pathEnd | testPath("legacy-summary")) {
        navigate("summary", replace = true)
      },
      path("summary") {
        div("Summary")
      },
      path("details") {
        div("Details")
      }
    )
  }
)
```

Note: using `testPath` is important here (using the regular `path` will mess up the relative url)

(see [example](/examples/navigate/live))

### Styling the links

`frontroute` provides a utility "modifier" to help style the links (active vs inactive): `navMod`.

```scala
a(
  navMod { (active: Signal[Boolean]) =>
    cls("active-link").toggle <-- active   
  }
)
```

`active` will contain `true` if the current page path starts with the `href` of the `a()`.

Another option is `navModExact`. It behaves similarly, but checks if the page path matches the `href` exactly.

```scala
a(
  navModExact { (active: Signal[Boolean]) =>
    cls("active-link").toggle <-- active   
  }
)
```


`navModFn` accepts a custom function to compare the URLs.

```scala
a(
  navModFn((currentLocation, linkLocation) => ??? ) { (active: Signal[Boolean]) =>
    cls("active-link").toggle <-- active   
  }
)
```

(see [example](/examples/navmod/live))
