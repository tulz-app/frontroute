package io.frontroute

import io.frontroute.testing._
import utest._

import scala.scalajs.js
import scala.scalajs.js.JSON

object ExtractPortTest extends TestBase {

  val tests: Tests = Tests {

    test("extractPort") {
      routeTest(
        route = probe =>
          extractPort { port =>
            testComplete {
              probe.append(port)
            }
          },
        init = locationProvider => {
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> List("443")
      }
    }

  }

}
