# Path-matching 

Path matchers are used to match a URL path against a defined "pattern" and (optionally) return an extracted value 
(similar to directives).

A path matcher can either "match" (and provide a value) or "reject".

`PathMatcher` is a function `List[String] => PathMatcherResult`, where `List[String]` represents the
segments of the path, and `PathMatcherResult` can be one of the following:

* `PathMatcherResult.Match(value, rest)`
* `PathMatcherResult.Rejected(rest)` – means that a matcher was able to consume a segment,
  but the subsequent transformation (`.filter`, `.collect`, etc) has failed.
* `PathMatcherResult.NoMatch` – means that a matcher needed to consume a segment, but the unmatched path was empty.

The way to use path matchers is to pass them as arguments to the path-matching directives like `path` or `pathPrefix`: see [built-in directives](/reference/directives)

Those directives will match when the corresponding `PathMatcher` matches (and provide the value as is), or reject when the path matcher rejects:

```scala
val d1: Directive[String] = pathPrefix(segment) // because segment is a PathMatcher[String]
// d1 will reject if the segment path matcher rejects - in this case, if the unmatched path is empty 

val d2: Directive0 = pathPrefix("some-prefix") // because "some-prefix" is (implicitly) a PathMatcher[Unit]
// d2 will reject if the segment("some-prefix") path matcher rejects 
```

Path matchers can be combined using the `/` combinator (the provided values, except `Unit`s, will be collected into a tuple):

```scala
val d: Directive[(String, Long)] = path("prefix" / segment / "page" / long)
```

See also: [built-in path matchers](/reference/path-matchers) and [path matcher combinators](/reference/path-matcher-combinators).

## Path matching process

When evaluating the route tree `frontroute` keeps and updates its internal state, which includes the `path`.

`path` is essentially a list of segments of the path in a `List[String]`. For example, when the path is `/users/12/posts/43/details` 
the initial `path` is set to `List("users", "12", "posts", "43", "details")`.

When one of the path matching directives matches (except `testPath` and `testPathPrefix`), it "consumes" the part from 
the "unmatched path"

<div class="bg-sky-200 px-8 py-2 text-sm">
It is actually the `PathMatcher` provided to the directive that does the matching and "consuming".
</div>

For example, with the above initial `path`, here's what the `path` will be during the route evaluation:

```scala
// path: List("users", "12", "posts", "43", "details")
firstMatch(
  
  // path: List("users", "12", "posts", "43", "details")
  // "public" != "users"
  //   --> rejects
  //   --> directive rejects
  pathPrefix("public") {
    // route evaluation never reaches here  
  }, 
  
  // path: List("users", "12", "posts", "43", "details")
  // "users" == "users"
  //   --> matches, provides Unit
  //   --> "users" is consumed (path: List("12", "posts", "43", "details"))
  // "all" != "12" 
  //   --> rejects 
  //   --> "..." / "..." rejects 
  //   --> directive rejects
  pathPrefix("users" / "all") { userId => 
    // route evaluation never reaches here 
    pathPrefix("something") { /* ... */ }
  },

  // path: List("users", "12", "posts", "43", "details")
  // "users" == "users" 
  //   --> matches and provides Unit
  //   --> "users" is consumed (path: List("12", "posts", "43", "details"))
  // segment matches any string 
  //   --> matches and provides "12"
  //   --> "12" is consumed (path: List("posts", "43", "details"))
  //   --> "..." / "..." matches, Unit and "12" are combined into just "12"
  //   --> provides "12"
  //   --> directive matches and provides "12"
  pathPrefix("users" / segment) { userId => // userId == "12" (provided by the path matcher and the directive) 
    // path: List("posts", "43", "details")
    // "posts" == "posts"
    //   --> matches and provides Unit
    //   --> "posts" is consumed (path: List("43", "details"))
    //   --> directive matches and provides Unit
    pathPrefix("posts") {
      // path: List("43", "details")
      firstMatch(
        // path: List("43", "details")
        // "all" != "43" 
        //   --> rejects
        //   --> directive rejects
        path("all") {
          // route evaluation never reaches here
        },

        // path: List("43", "details")
        // long matches "43"
        //   --> matches and provides (43: Long)
        //   --> "43" is consumed (path: List("details"))
        //   --> directive matches and provides (43: Long)
        pathPrefix(long) { postId => // postId: Long == 43 (provided by the path matcher and the directive)
          // path: List("details")
          // no match 
          //   --> rejects
          //   --> directive rejects
          pathEnd {
            // route evaluation never reaches here
          },

          // path: List("details")
          // "details" == "details" AND no more unmatched segments
          //   --> matches and provides Unit
          //   --> "details" is consumed (path: List.empty)
          //   --> directive matches and provides Unit
          path("details") {
            // path: List.empty
            // end of the evaluation, the provided element will be rendered
            div("Render me!")
          }
        }
      )      
    }
  }
)
```
