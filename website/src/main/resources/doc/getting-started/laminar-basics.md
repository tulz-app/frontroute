We will start with defining the "view" part of our application. The view is a tree of elements, like `<div>`-s and `<span>`-s:

```scala
import com.raquo.laminar.api.L.*

val myApp: Element = 
  div(
    span("Hello, "),
    span("world!")
  )
```

The tree we've just described here corresponds directly to a tree of native DOM elements that will be 
rendered on the page when `myApp` is "mounted".

#### Mounting the app

In order to mount the top-level element at the start of our application, we first need to have an existing placeholder in 
the DOM. Usually we put it directly into the `.html` file:

```html
<!DOCTYPE html>
<head><title>My App</title></head>
<body>
    <div id="app"></div>
</body>
</html>
```

Here, we designate the `<div id="app">` to be the placeholder for our app.

Now we can call Laminar's `render` function:

```scala
import com.raquo.laminar.api.L.*

object Main {

  val myApp = ...
  
  def main(args: Array[String]): Unit = {
    val appContainer = org.scalajs.dom.document.querySelector("#app")
    render(
      appContainer,
      myApp
    )
  }

}
```

And that's all we need to see our "Hello, world!" rendered in the browser.

### Modifiers

In Laminar, building the view revolves around `Element`-s and `Modifier`-s.

`Element`-s correspond to the DOM elements (HTML or SVG), like `<div>` or `<input>`.

But what is a modifier? From the Laminar [documentation](https://laminar.dev/documentation#modifiers):

> Conceptually, it's a function that you can apply to an element El to modify it in some way.

And in order to apply a `Modifier` to an element, we "nest" it inside the element: 

```scala
div(modifier1, modifier2, ...)
```

Let's look at the tree we've defined above:

```scala
div(
  span("Hello, "),
  span("world!")
)
```

We create a `div()` element (which creates a `<div>` DOM element), with two nested `span()` elements (which create two `<span>` 
DOM elements). We can nest elements inside parent elements because an `Element` is also a `Modifier` – which, when applied to a parent,
inserts its DOM element into the parent's DOM element.

As the result, we start with an empty `div()`, and by applying the `span()` elements as modifiers,
we get the corresponding `<span>` DOM elements inserted into the parent `<div>` DOM element.

To each of the `span()` elements we also applied a modifier – `"Hello, "` and `"world!"` (strings get implicitly
converted into `Modifier`-s, which insert text nodes into the parent element when applied). 

So when we mount the `myApp` element, it will render a `<div>` element with two `<span>` elements (each with a text node) inside it:

```html
<div><span>Hello, </span><span>world!</span></div>
```

There is a lot of different modifiers in Laminar, serving all kinds of purposes: from simple ones that merely change an attribute 
of an element, to complex and powerful modifiers which can, for example, render a dynamically changing list of children.

A few simple examples:

* add a CSS class to an element:
    ```scala
    div(
      cls := "my-css-class"
    )
    ```

* set a `href` for an `<a>` element:
    ```scala
    a(href := "https://frontroute.dev")
    ```
  
* append a list of elements:
    ```scala
    div(
      (1 to 5).map { i => span(s"span $i")  }
    )
    ```
  
An example of a more powerful modifier is the `child <-- ...`. 

When you have a signal of an `Element` (`Signal[Element]`), you can insert the element contained in that signal into the parent element,
and the child element will be replaced whenever the signal changes:

```scala
val childElement: Signal[Element] = ???
div(
  child <-- childElement
)
```

Another modifier is `child.maybe <-- ...`. It acts similarly, except when the signal contains a `None`, nothing  
will be inserted into the parent:

```scala
val maybeChildElement: Signal[Option[Element]] = ???
div(
  child.maybe <-- maybeChildElement
)
```

Now that we have the basics covered, let's add [some routes](first-routes) to our application.