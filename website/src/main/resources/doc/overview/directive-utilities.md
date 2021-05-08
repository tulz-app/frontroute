# Directive convenience utilities

### `some: Directive[Option[L]]`

Transforms a `Directive[L]` into a `Directive[Option[L]]`, providing this directive's value inside `Some`.

```scala
param("some-param").some { paramValueInsideSome => 
  /* ... */
}
```

### `none[R]: Directive[Option[R]]`

Transforms a `Directive[L]` into a `Directive[Option[L]]`, providing `None`.

```scala
param("some-param").none { alwaysNone => 
  /* ... */
}
```

### `mapTo[R](otherValue: => R): Directive[R]`

Transforms a `Directive[L]` into a `Directive[R]`, providing the given value instead of the value provided by this directive.

```scala
param("some-param").mapTo(123) { always123 => 
  /* ... */
}
```

### `def mapOption[R](f: A => R): Directive[Option[R]]`

Only for a `Directive[Option[A]]`.

Maps the value inside the `Option`.

