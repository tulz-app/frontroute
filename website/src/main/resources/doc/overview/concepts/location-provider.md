# Location Provider

`LocationProvider` is defined the following way:

```scala
trait LocationProvider {
  def stream: EventStream[RouteLocation]
}
```

When running a route, `frontroute` subscribes to the provided stream and matches the 
provided `RouteLocation` against the `Route`.

To get a browser location provider you can use the built-in `LocationProvider.browser`.

It accepts the following parameters:

* `popStateEvents: EventStream[dom.PopStateEvent]`

With Laminar you can provide `windowEvents.onPopState` directly

* `setTitleOnPopStateEvents: Boolean`
Default: `true`

If `true`, the `document.title` will be set to the value passed to the corresponding `pushState`. 
  
* `updateTitleElement: Boolean`
Default: `true`
  
If `true`, the `<title>` element in the dom will be updated as well.

* `ignoreEmptyTitle: Boolean`
Default: `false`

## Custom location provider

When not using Laminar, or in tests, you can use the `LocationProvider.custom`, passing it an `EventStream[String]`.

`CustomLocationProvider` will parse the strings emitted by the stream as URLs.
