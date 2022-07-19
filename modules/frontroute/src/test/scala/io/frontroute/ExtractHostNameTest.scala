package io.frontroute

import io.frontroute.testing._
import utest._

import scala.scalajs.js
import scala.scalajs.js.JSON

object ExtractHostNameTest extends TestBase {

  val tests: Tests = Tests {

    test("extractHostname") {
      routeTest(
        route = probe =>
          extractHostname { hostname =>
            testComplete {
              probe.append(hostname)
            }
          },
        init = locationProvider => {
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> List("", "test.nowhere")
      }
    }

  }

}
