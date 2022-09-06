package io.frontroute.pathDirectives

import io.frontroute.testing.TestBase
import io.frontroute._

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
