package io.frontroute.pathDirectives

import io.frontroute._
import io.frontroute.testing.TestBase
import utest._

object SimplePathEndTest extends TestBase {

  val tests: Tests = Tests {

    test("simple pathEnd") {
      println("simple pathEnd")
      routeTest(
        route = probe =>
          pathEnd {
            testComplete {
              probe.append("end")
            }
          },
        init = locationProvider => {
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> List("end")
      }
    }

  }

}
