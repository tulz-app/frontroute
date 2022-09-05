# Disjunction

Directives can be combined using the `|` operator.

The resulting directive will match if any of the combined directives match.

The resulting directive will provide the value provided by the first of the combined directives
that matches (the remaining directives are not evaluated).

```scala
(path("page-1") | path("page-1-alternative")) { 
  ??? 
}
```

```scala
(param("param-1") | param("param-2") | param("param-3")) { valueFromParam1or2or3 =>  
  ??? 
}
```
