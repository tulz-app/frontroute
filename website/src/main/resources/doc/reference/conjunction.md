# Conjunction

Directives can be combined using the `&` operator.

The resulting directive will match only if both the combined directives match.

The resulting directive will provide the values provided by the combined directives 
in a tuple (except `Unit`s)

If only one of the combined directives provides a non-`Unit` value, and it's a scalar – the conjunction will provide 
a scalar as well (it will not wrap it into a 1-tuple).

If both combined directives provide a `Unit` — the conjunction will provide a `Unit` as well.

See the [tuplez](https://github.com/tulz-app/tuplez/) library for more details.

```scala
(path("page-1") & param("param-1") & param("param-2")) { (param2, param2) =>
    complete { /*...*/ } 
}
```

Equivalent to "nesting": 

```scala
path("page-1") { 
  param("param-1") { param1 => 
    param("param-2") { param2 =>
      complete { /*...*/ }
    }
  }
}

```
