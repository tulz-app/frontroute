# Custom directives 

In order to create a custom directive, you should start with an existing one and build
your directive using the combinators that directives provide:

* `map[R](f: L => R): Directive[R]`
* `flatMap[R](next: L => Directive[R]): Directive[R]`
* `collect[R](f: PartialFunction[L, R]): Directive[R]`
* `filter(predicate: L => Boolean): Directive[L]`

## Example

Say, you wanted to check if the matched segment (part of the URI path delimited by `/`) is a number.

Let's create an `isNumber` directive for that, we will use it like this:

```scala
val route = concat(
  // ...
  path("user" / segment) { userId =>
    isNumber(userId) { userIdAsInt =>
      userByIdPage(userId) 
    }
  }
  // ...
)
```

> For this particular case, using the `long` path matcher would make much more sense, but let's try this anyway,
to keep things simple.

Let's define the directive now:

```scala
  def isNumber(s: String): Directive[Int] = 
    Try(s.toInt) match {
      case Success(int) => provide(int)
      case Failure(_) => reject
    }  
```

> Now, this is an extremely simple example, but most directives, no matter how complicated, can be
derived as easily from the more powerful existing directives and more powerful combinators (like, for example, 
the `signal` directive or the `flatMap` combinator).
