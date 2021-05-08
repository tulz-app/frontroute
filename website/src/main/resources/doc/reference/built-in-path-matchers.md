See also: [Path Matcher](/overview/path-matcher).

## `segment: PathMatcher[String]`

* Matches (and consumes*) a single segment of the unmatched path.
* Provides the segment as the value.
* Rejects if there are no more unmatched segments left.

> "consumes" – this means the nested route will see the unmatched path without the consumed prefix

```scala
path(segment) { (s: String) => 
  ???
}
```

## `long: PathMatcher[Long]`

* Matches (and consumes) a single segment of the unmatched path if it can be parsed as a `Long`.
* Provides the parsed value.
* Rejects if the segment can't be parsed as a `Long`.
* Rejects if there are no more unmatched segments left.

```scala
path(long) { (l: Long) => 
  ???
}
```

## `double: PathMatcher[Double]`

* Matches (and consumes) a single segment of the unmatched path if it can be parsed as a `Double`.
* Provides the parsed value.
* Rejects if the segment can't be parsed as a `Double`.
* Rejects if there are no more unmatched segments left.

```scala
path(double) { (d: Double) => 
  ???
}
```

## `segment(s: String): PathMatcher0`

* Matches (and consumes) a single segment if it is equal to the provided string.
* Rejects if the segment doesn't match the provided string.
* Doesn't provide a value.
* Rejects if there are no more unmatched segments left.

> `PathMatcher0` is an alias for `PathMatcher[Unit]`

```scala
path(segment("some-page")) {  
  ???
}
```

Strings are implicitly converted into a `segment` path matcher.

The following is equivalent to the above example:

```scala
path("some-page") {
  ???  
}
```

## `regex(e: Regex): PathMatcher[Regex.Match]`

* Matches (and consumes) a single segment of the unmatched path if it matches 
the regular expression.
* Rejects if the segment doesn't match the provided regex.
* Rejects if there are no more unmatched segments left.
* Provides the corresponding `Regex.Match`.


```scala
path("some-page") { (m: Regex.Match) =>
  ???
}
```

`Regex`es are implicitly converted into a `regex` path matcher.

The following is equivalent to the above example:

```scala
path("\\w".r) { (m: Regex.Match) =>
  ???  
}
```

## `fromTry[V](t: Try[V]): PathMatcher[V]`

Doesn't consume any segments from the URI path.
Matches or rejects depending on whether the `Try` is successful.

## `tryParse[V](t: => V): PathMatcher[V]`

Wraps the computation of `t` in a `Try` and passes it to `fromTry`.
Effectively catching exceptions and rejecting if any.

## `PathMatcher.unit: PathMatcher[Unit]`

Always matches.

## `PathMatcher.provide[V](v: V): PathMatcher[V]`

Always matches, provides the specified value.

## `PathMatcher.fail[T](msg: String): PathMatcher[T]`

Always rejects.
