## `.map`

```scala
def map[R](f: L => R): Directive[R]
```

Transforms a `Directive[L]` into a `Directive[R]`.

```scala
param("some-param").map(_.toUpperCase) { upperCaseParam => ??? }
```


---


## `.emap`

```scala
def emap[R](f: L => Either[Any, R]): Directive[R]
```

Transforms a `Directive[L]` into a `Directive[R]`. If the returned `Either` is a `Left`, the `.emap` directive will
reject (ignoring the left value).

If the original directive rejects, the `.none` directive will also reject.


---


## `.opt`

```scala
def opt: Directive[Option[L]]
```

Transforms a `Directive[L]` into a `Directive[Option[L]]`.

If the original directive matches with value `v`, the `.opt` directive will also match and will provide `Some(v)`.

If the original directive rejects, the `.opt` directive will match nevertheless and will provide `None`.


---


## `.some`

```scala
def some: Directive[Option[L]]
```

Transforms a `Directive[L]` into a `Directive[Option[L]]`.

The `.some` directive will provide the original directives output inside `Some`.

If the original directive rejects, the `.some` directive will also reject.

```scala
param("some-param").some { paramValueInsideSome => ??? }
```


---


## `.none`

```scala
def none[A]: Directive[Option[A]]
```

Transforms a `Directive[L]` into a `Directive[Option[A]]`.

If the original directive matches, the `.none` directive will match as well and will provide `Option.empty[R]`.

If the original directive rejects, the `.none` directive will also reject.


```scala
param("some-param").none { alwaysNone => ??? }
```


---


## `.mapTo`

```scala
def mapTo[R](otherValue: => R): Directive[R]
```

Transforms a `Directive[L]` into a `Directive[R]`, providing the given value instead of the value provided by the original directive.

```scala
param("some-param").mapTo(123) { always123 => ??? }
```


---


## `.mapOption`

```scala
def mapOption[R](f: A => R): Directive[Option[R]]
```

Only for `Directive[Option[A]]`.

Maps the value inside the `Option`.


---


## `.collectOption`

```scala
def collectOption[R](f: A => R): Directive[Option[R]]
```

Only for `Directive[Option[A]]`.

Collects the value inside the `Option`.


---


## `.default`

```scala
def default(v: A): Directive[Option[A]]
```

Only for `Directive[Option[A]]`. 

Transforms a `Directive[Option[A]]` into a `Directive[A]` using the provided default value.

