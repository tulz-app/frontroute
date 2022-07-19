package io.frontroute.pathDirectives

import io.frontroute.testing.TestBase
import io.frontroute._
import utest._

object TestPathTest extends TestBase {

  val tests: Tests = Tests {

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
        probe.toList ==> List("a/b/c/d")
      }
    }

  }

}
