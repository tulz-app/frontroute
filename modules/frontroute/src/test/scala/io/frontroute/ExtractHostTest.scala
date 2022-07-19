package io.frontroute

import io.frontroute.testing._
import utest._

import scala.scalajs.js
import scala.scalajs.js.JSON

object ExtractHostTest extends TestBase {

  val tests: Tests = Tests {

    test("extractHost") {
      routeTest(
        route = probe =>
          extractHost { host =>
            testComplete {
              probe.append(host)
            }
          },
        init = locationProvider => {
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> List("", "test.nowhere:443")
      }
    }

  }

}
