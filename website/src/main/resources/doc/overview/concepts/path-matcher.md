# Path matchers 

Path matchers are used to match a URL path against a defined "pattern" and (optionally) return an extracted value 
(similar to directives).

A path matcher can either "match" (and provide a value) or "reject".

`PathMatcher` is a function `List[String] => PathMatcherResult`, where `List[String]` represents the
unmatched path and `PathMatcherResult` can be one of the following:

* `PathMatcherResult.Match(value, rest)`
* `PathMatcherResult.Rejected(rest)` – means that a matcher was able to consume a segment,
  but the subsequent transformation (`.filter`, `.collect`, etc) has failed.
* `PathMatcherResult.NoMatch` – means that a matcher needed to consume a segment, but the unmatched path was empty.

The way to use path matchers is to pass them as arguments to the path-matching directives like `path` or `pathPrefix`: see [built-in directives]({{sitePrefix}}/reference/directives)

Those directives will be "matching" with the values provided by the corresponding path matchers as is, or rejecting if the path matcher rejects:

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

See also: [built-in path matchers]({{sitePrefix}}/reference/path-matchers) and [path matcher combinators]({{sitePrefix}}/reference/path-matcher-combinators).

## Path matching process

When evaluating the route tree `frontroute` keeps and updates its internal state, which includes the "unmatched path".

Unmatched path is essentially a `List[String]`, and it is initially set to
`location.pathname.dropWhile(_ == '/').split('/').toList.dropWhile(_.isEmpty)`.

For example, when the path is `/users/12/posts/43/details` the initial "unmatched path" is set
to `List("users", "12", "posts", "43", "details")`.

When one of the path matching directives matches (except `testPath` and `testPathPrefix`), it "consumes" the part from 
the "unmatched path"

> It is actually the `PathMatcher` provided to the directive that does the matching and "consuming".

For example, with the above initial "unmatched path", here's what the "unmatched path" will be during the route evaluation:

```scala
// unmatchedPath: List("users", "12", "posts", "43", "details")
concat(
  
  //  unmatchedPath: List("users", "12", "posts", "43", "details")
  // "public" != "users"
  //   --> rejects
  //   --> directive rejects
  pathPrefix("public") { /* ... */ }, 
  
  // unmatchedPath: List("users", "12", "posts", "43", "details")
  // "users" == "users"
  //   --> matches, provides Unit
  //   --> "users" is consumed (unmatchedPath: List("12", "posts", "43", "details"))
  // "all" != "12" 
  //   --> rejects 
  //   --> " ... / ... " rejects 
  //   --> unmatchedPath is rolled back
  //   --> directive rejects
  pathPrefix("users" / "all") { userId => 
    // route evaluation never reaches here 
    pathPrefix("something") { /* ... */ }
  },

  // unmatchedPath: List("users", "12", "posts", "43", "details")
  // "users" == "users" 
  //   --> matches and provides Unit
  //   --> "users" is consumed (unmatchedPath: List("12", "posts", "43", "details"))
  // segment matches any string 
  //   --> matches and provides "12"
  //   --> "12" is consumed (unmatchedPath: List("posts", "43", "details"))
  //   --> " ... / ... " matches, Unit and "12" are combined into just "12"
  //   --> provides "12"
  //   --> directive matches and provides "12"
  pathPrefix("users" / segment) { userId => // userId == "12"
    // unmatchedPath: List("posts", "43", "details")
    // "posts" == "posts"
    //   --> matches and provides Unit
    //   --> "posts" is consumed (unmatchedPath: List("43", "details"))
    //   --> directive matches and provides Unit
    pathPrefix("posts") {
      // unmatchedPath: List("43", "details")
      concat(
        // unmatchedPath: List("43", "details")
        // "all" != "43" 
        //   --> rejects
        //   --> directive rejects
        path("all") { /* ... */ },

        // unmatchedPath: List("43", "details")
        // long matches "43"
        //   --> matches and provides 43: Long
        //   --> "43" is consumed (unmatchedPath: List("details"))
        //   --> directive matches and provides 43: Long
        pathPrefix(long) { postId => // postId: Long == 43
          // unmatchedPath: List("details")
          // no match 
          //   --> rejects
          //   --> directive rejects
          pathEnd { /* ... */ },

          // unmatchedPath: List("details")
          // "details" == "details" AND no more unmatched segments
          //   --> matches and provides Unit
          //   --> "details" is consumed (unmatchedPath: List.empty)
          //   --> directive matches and provides Unit
          path("details") {
            // unmatchedPath: List.empty
            // complete terminates the evaluation, the provided code block will get executed
            complete {
              dom.console.log("user post details - match")
            }
          }
        }
      )      
    }
  }
)
```
