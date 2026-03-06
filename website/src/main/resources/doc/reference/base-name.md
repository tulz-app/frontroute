## `baseName`

Starting with version `0.20.0`, `frontroute` supports mounting routes under a specified baseName.

### What is a baseName

`baseName` is effectively a prefix in the URL path, which is considered to be the root of the application.
All routes are defined relative to this prefix, and all links under the router a automatically rewritten to include the specified `baseName`.

## Initialization

The existing "entry point" functions now have overloads which allow specifying the `baseName`. 

```scala
import frontroute.*

routes(baseName = "/example-basename") {
  pathPrefix("page-1") { // will match the /example-basename/page-1 path
    div("page 1")
  }  
}
```

```scala
import frontroute.*

div(
  initRouting(baseName = "/example-basename"),
  pathPrefix("page-1") { // will match the /example-basename/page-1 path
    div("page 1")
  }
)
```

## href and src rewriting

By default, `frontroute` will rewrite all your href's and src's (mounted under the routes):
* will prepend the `baseName`
* will resolve the relative paths (equivalent to the existing [relativeHref](/getting-started/links-and-navigation#relativeHref) modifier)

```scala
import frontroute.*

routes(baseName = "/example-basename") {
  pathPrefix("page-1") { // will match the /example-basename/page-1 path
    path("summary") {
      div(
        // /example-basename/sub-page
        a(href := "/sub-page", "Root link"),
        // /example-basename/page-1/summary/sub-page
        a(href := "sub-page", "Sub link"),  
        // /example-basename/page-1/sibling-page
        a(href := "../sibling-page", "Test link"),  
      )
    }
  }  
}
```

## Options

The automatic rewriting can be configured or disabled using the new `FrontrouteOptions` parameter:

```scala
import frontroute.*

routes(baseName = "/example-basename", FrontrouteOptions.default.copy(
  rewriteUrls = false,
)) {
// ...
}
```

```scala
import frontroute.*

routes(baseName = "/example-basename", FrontrouteOptions.default.copy(
  rewriteAnchorHref = true,
  rewriteLinkHref = false,
  rewriteImageSrc = false,
  rewriteScriptSrc = false,
  rewriteIframeSrc = false
)) {
// ...
}
```

## Preserving pre-0.20.x behavior

In order to revert to the pre-0.20.x behaviour, specify `baseName=""` and `rewriteUrls = false`. 