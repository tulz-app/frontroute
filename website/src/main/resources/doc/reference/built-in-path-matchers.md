See also: [path-matching](/reference/under-the-hood/path-matching).

## `segment: PathMatcher[String]`

If the path is non-empty:
* matches (and consumes*) a single segment of the path,
* provides the segment as the value.

Rejects if there are no more segments left.

<div class="bg-sky-200 px-8 py-2 text-sm">
"consumes" – this means the subsequent directives and nested routes will see the unmatched path without the consumed prefix.
</div>

```scala
path(segment) { (s: String) => 
  ???
}
```

## `long: PathMatcher[Long]`

If the path is non-empty:

* matches (and consumes) a single segment of the path if it can be parsed as a `Long`,
* provides the parsed `Long` value;
* rejects if the segment cannot be parsed as a `Long`.

Rejects if there are no more segments left.

```scala
path(long) { (l: Long) => 
  ???
}
```

## `double: PathMatcher[Double]`

If the path is non-empty:

* matches (and consumes) a single segment of the unmatched path if it can be parsed as a `Double`,
* provides the parsed `Double` value;
* rejects if the segment cannot be parsed as a `Double`.

Rejects if there are no more segments left.

```scala
path(double) { (d: Double) => 
  ???
}
```

## `segment(s: String): PathMatcher0`

If the path is non-empty:

* matches (and consumes) a single segment if it is equal to the provided string,
* doesn't provide a value;
* rejects if the segment doesn't match the provided string.

Rejects if there are no more segments left.

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

## `segment(oneOf: Seq[String]): PathMatcher[String]` and `segment(oneOf: Set[String]): PathMatcher[String]` 

If the path is non-empty:

* matches (and consumes) a single segment if it is contained in the provided `oneOf` collection.
* provides the matched segment;
* rejects if the segment doesn't match the provided string.

Rejects if there are no more segments left.

```scala
path(segment(Seq("some-page", "another-page"))) { matched =>   
  ???
}
```

## `regex(e: Regex): PathMatcher[Regex.Match]`

If the path is non-empty:

* matches (and consumes) a single segment of the unmatched path if it matches 
the regular expression,
* provides the corresponding `Regex.Match`;
* rejects if the segment doesn't match the provided regex.

Rejects if there are no more segments left.


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

## `PathMatcher.unit: PathMatcher0`

Always matches.

## `PathMatcher.provide[V](v: V): PathMatcher[V]`

Always matches, provides the specified value.

## `PathMatcher.fail[T]: PathMatcher[T]`

Always rejects.
