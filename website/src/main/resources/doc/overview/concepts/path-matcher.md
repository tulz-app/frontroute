# Path matchers 

Path matchers are used to match a URL path against some defined "patterns" and (optionally) return an extracted value 
(similar to directives). The way to use them is to pass them as arguments to the path-matching directives:

* [path](/reference/path)
* [pathPrefix](/reference/path-prefix)

Those directives will be providing the values provided by their path matchers as is:

```scala
val d1: Directive[String] = pathPrefix(segment) // because segment is a PathMatcher[String]
val d2: Directive0 = pathPrefix("some-prefix") // because "some-prefix" is (implicitly) a PathMatcher[Unit]
```

Path matchers can be combined using the `/` combinator (the provided values, except `Unit`s, will be collected into a tuple):

```scala
val d: Directive[(Stirng, Int)] = path("prefix" / segment / "page" / int)
```

Path matchers also provide the following combinators:

* `map` (or `as`)
* `flatMap`
* `filter`
* `collect`

## `unary_!`

```scala
path(!"wrong-path") // will match when the path is NOT /wrong-path
```

## `void`

```scala
path(int.void) // will match if the segment can be parsed as an int, but will return Unit
``` 

See [the reference](/reference/path-matchers) for a list of all built-in path matchers. 

## Path matching process

When evaluating the route tree `frontroute` keeps and updates its internal state, which includes the "unmatched path".

Unmatched path is a essentially a `List[String]`, and is initially set to `location.pathname.dropWhile(_ == '/').split('/').toList.dropWhile(_.isEmpty)`.

For example, when the path is `/users/12/posts/43/details` the initial "unmatched path" is set to `List("users", "12", "posts", "43", "details")`.

When one of the path matching directives matches, it "consumes" the part of the "unmatched path"

> It is actually the `PathMatcher` provide to the directive that does the matching and "consuming".

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
