package io.frontroute.pathDirectives

import io.frontroute.testing.TestBase
import io.frontroute._
import utest._

object PathWithOneOfAndRecoverTest extends TestBase {

  val tests: Tests = Tests {

    test("path with oneOf path matcher and recover") {
      routeTest(
        route = probe =>
          path(segment(Set("a", "b")).recover("default")) { str =>
            testComplete {
              probe.append(str)
            }
          },
        init = locationProvider => {
          locationProvider.path("a")
          locationProvider.path("b")
          locationProvider.path("c")
        }
      ) { probe =>
        probe.toList ==> List("a", "b", "default")
      }
    }

  }

}
