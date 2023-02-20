package frontroute.pathDirectives

import frontroute.testing.TestBase
import frontroute._

class TestPathTest extends TestBase {

  test("test path") {
    routeTest(
      route = probe =>
        testPath("a" / "b" / "c" / "d") {
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
