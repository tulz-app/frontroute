### Laminar

Following this tutorial will be easier if you're already familiar with [Laminar](https://laminar.dev), but
I'll cover the basics as we go. 

### Imports

Let's start by adding the imports we need:

```scala
import com.raquo.laminar.api.L.*
import io.frontroute.*
import org.scalajs.dom
```

### Basic application

We will start with defining the "view" part of our application, which is a tree of elements, like `<div>`-s and `<span>`-s:

```scala
val myApp: Element = 
  div(
    span("Hello, "),
    span("world!")
  )
```

The tree we've just described here corresponds directly to a tree of native DOM elements that will be 
added to the page when this `Element` is "mounted".

#### Mounting the view

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

Here, we designate the `<div id="app">` to be this placeholder.

Next, we call Laminar's `render` function:

```scala
object Main {

  def main(args: Array[String]): Unit = {
    val appContainer = dom.document.querySelector("#app")
    com.raquo.laminar.api.L.render(
      appContainer,
      myApp
    )
  }

}
```

And that's all we need to see our "Hello, world!" rendered in the browser.

### Modifiers

In laminar, things revolve around `Elements` and `Modifiers` (in fact, `Elements` are `Modifiers`, too).

What is a modifier? From the [Laminar docs](https://laminar.dev/documentation#modifiers):

> Conceptually, it's a function that you can apply to an element El to modify it in some way.

Let's look at the tree we've defined above:

```scala
div(
  span("Hello, "),
  span("world!")
)
```

Here, we have one `div()` element, with two modifiers applied to it – the two `span()`-s. `span()` creates an `Element`, but 
as we already know, `Element`-s are `Modifiers`, and applying it means "append this element as a child".

As the result, we start with an empty `div()`, and but passing in the `span()` elements/modifiers, we get those `span`-s appended as 
children to the `div`.

Same thing happens with `span()`-s: for each of them we also provided a modifier – `"Hello, "` and `"world!"`. Strings are implicitly
converted into `Modifiers`, which append text nodes to the element when applied. Thus, we ended up having two `span`-s, each with 
a text node, inside a `div`:

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

When you have a signal of an `Element` (`Signal[Element]`), you can append the element inside that signal to another element,
and the child element will be replaced whenever the element the signal holds changes:

```scala
val childElement: Signal[Element] = ???
div(
  child <-- childElement
)
```

Another modifier – `child.maybe <-- ...` – acts similarly, except when the signal contains a `None`, the child element 
will be removed from the parent:

```scala
val maybeChildElement: Signal[Option[Element]] = ???
div(
  child.maybe <-- maybeChildElement
)
```

### frontroute

`frontroute`'s `Routes` (which are `Modifiers`, too) act similarly to the `child.maybe <-- ...` modifier: 
* when there is a "match", the corresponding element is appended to the parent,
* when there is no "match", the element is removed.

Let's add some routes to our application.