package io.frontroute.pathDirectives

import io.frontroute.testing.TestBase
import io.frontroute._
import utest._

object RevisitPreviousMatchTest extends TestBase {

  val tests: Tests = Tests {

    test("revisit previous match") {
      routeTest(
        route = probe =>
          concat(
            pathEnd {
              testComplete {
                probe.append("end")
              }
            },
            path("path1") {
              testComplete {
                probe.append("path1")
              }
            }
          ),
        init = locationProvider => {
          locationProvider.path()
          locationProvider.path("path1")
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> List("end", "path1", "end")
      }
    }

  }

}
