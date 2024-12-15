package frontroute.pathDirectives

import frontroute.testing.TestBase
import frontroute.*

class TestPathPrefixTest extends TestBase {

  test("test path prefix") {
    routeTest(
      route = probe =>
        testPathPrefix("a" / "b") {
          extractUnmatchedPath { unmatched =>
            testComplete {
              probe.append(unmatched.mkString("/"))
            }
          }
        },
      init = locationProvider => {
        locationProvider.path("a", "b", "c", "d")
      }
    ) { probe =>
      probe.toList shouldBe List("a/b/c/d")
    }
  }

}
