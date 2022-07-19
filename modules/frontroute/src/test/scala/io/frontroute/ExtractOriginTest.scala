package io.frontroute

import io.frontroute.testing._
import utest._

import scala.scalajs.js
import scala.scalajs.js.JSON

object ExtractOriginTest extends TestBase {

  val tests: Tests = Tests {

    test("extractOrigin") {
      routeTest(
        route = probe =>
          extractOrigin { origin =>
            testComplete {
              probe.append(origin.getOrElse("---"))
            }
          },
        init = locationProvider => {
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> List("https://test.nowhere:443")
      }
    }

  }

}
