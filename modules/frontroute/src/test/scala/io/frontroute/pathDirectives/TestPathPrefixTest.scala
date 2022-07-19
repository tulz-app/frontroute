package io.frontroute.pathDirectives

import io.frontroute.testing.TestBase
import io.frontroute._
import utest._

object TestPathPrefixTest extends TestBase {

  val tests: Tests = Tests {

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
        probe.toList ==> List("a/b/c/d")
      }
    }

  }

}
