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
import io.frontroute._

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
  import io.frontroute._

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
