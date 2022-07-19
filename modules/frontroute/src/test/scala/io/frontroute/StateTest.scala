package io.frontroute

import io.frontroute.testing._
import utest._
import scala.scalajs.js
import scala.scalajs.js.JSON

object StateTest extends TestBase {

  val tests: Tests = Tests {

    test("state") {
      var counter = 0
      routeTest(
        route = probe =>
          state {
            counter = counter + 1
            counter
          } { state =>
            extractUnmatchedPath { _ =>
              testComplete {
                probe.append(state.toString)
              }
            }
          },
        init = locationProvider => {
          locationProvider.path("state-test-1")
          locationProvider.path("state-test-2")
          locationProvider.path("state-test-3")
          locationProvider.path("state-test-4")
        }
      ) { probe =>
        probe.toList ==> List("1", "1", "1", "1")
      }
    }

  }

}
