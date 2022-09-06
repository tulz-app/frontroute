`TODO`


### `reject`

```scala
val reject: Route
```

Returns a `Route` that always rejects.




# Custom directives 

In order to create a custom directive, you should start with an existing one and build
your directive using the combinators that directives provide:

* `map[R](f: L => R): Directive[R]`
* `flatMap[R](next: L => Directive[R]): Directive[R]`
* `collect[R](f: PartialFunction[L, R]): Directive[R]`
* `filter(predicate: L => Boolean): Directive[L]`

## Example

Say, you wanted to check if the next segment in the path is a number.

Let's create an `asNumber` directive which we will use like this:

```scala
path("user" / segment) { userId =>
  asNumber(userId) { userIdAsInt =>
    userByIdPage(userId) 
  }
}
```

<div class="bg-sky-200 px-8 py-2 text-sm">
For this particular case, using the `long` path matcher would make much more sense, but let's try this anyway,
to keep things simple.
</div>

```scala
def asNumber(s: String): Directive[Int] = 
  Try(s.toInt) match {
    case Success(int) => provide(int)
    case Failure(_) => reject
  }  
```

<div class="bg-sky-200 px-8 py-2 text-sm">
This is an extremely simple example, but most directives, no matter how complicated, can be
derived as easily from the more powerful existing directives and more powerful combinators (like, for example, 
the `signal` directive or the `flatMap` combinator).
</div>

[Live example](/examples/custom-directives) 
