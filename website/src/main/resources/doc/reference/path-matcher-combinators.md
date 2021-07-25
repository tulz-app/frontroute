None of the following combinators will have any effect if the underlying matcher results in `NoMatch`.

Most of the combinators (except `.recover` and `!`) will also have no effect if the underlying matcher results in `Rejected`.

## `.map[V](f: T => V): PathMatcher[V]`

If the underlying matcher matches, the resulting matcher provides the transformed value.

## `.mapTo[V](value: => V): PathMatcher[V]`

If the underlying matcher matches, the resulting matcher provides the `value` (ignoring the value provided by the underlying matcher).

## `.emap[V](f: T => Either[String, V])`

If the underlying matcher matches, `f` is applied to the provided value.
If it returns a `Right(value)` – the resulting matcher provides `value`, if `f` returns a `Left`, the resulting matcher
rejects (the value inside the `Left` is ignored).

## `.tryParse[V](f: T => V): PathMatcher[V]`

Same as map, but `f` is wrapped inside a `Try`. If an exception is thrown, the resulting matcher will reject.

## `.flatMap[V](f: T => PathMatcher[V])`

Applies the underlying path matcher, if it matches – applies the next path matcher.

## `.filter(f: T => Boolean): PathMatcher[T]`

Checks if a condition holds for the provided value. If not, the path matcher rejects.

```scala
path(segment.filter(_.forall(_.isDigit))) { value => // will match only if the path segment contains digits only 
  // ...  
}
```

## `.collect[V](f: PartialFunction[T, V]): PathMatcher[V]`

If `f` is defined at the provided value, the result of applying `f` to it is provided. If not, the path matcher rejects.

```scala
path(long.collect {
  case v if v < 1000 => v 
} ) { value => // will match only if the path segment parsed as Long is < 1000 
  // ...  
}
```

## `.recover[V >: T](default: => V): PathMatcher[V]`

If the underlying matcher rejects, matches and provides the `default` value instead.

```scala
path(long.recover(0)) { value => // if the path segment cannot be parsed as a Long, will recover to 0
  ???  
}
```

## `.unary_! : PathMatcher[Unit]`

The resulting matcher matches if the underlying matcher rejects and rejects if the underlying matcher matches. 

```scala
pathPrefix(!"wrong-path") { // will match when the path is NOT /wrong-path
  // path does not start with 'wront-path'
  ???
}
```

## `void`

Replaces the provided value with Unit.

```scala
path(long.void) { // will match if the segment can be parsed as a long, but will provide Unit
  ???
}
``` 

