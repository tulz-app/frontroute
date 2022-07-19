package io.frontroute

import io.frontroute.testing._
import utest._

import scala.scalajs.js
import scala.scalajs.js.JSON

object ExtractProtocolTest extends TestBase {

  val tests: Tests = Tests {

    test("extractProtocol") {
      routeTest(
        route = probe =>
          extractProtocol { protocol =>
            testComplete {
              probe.append(protocol)
            }
          },
        init = locationProvider => {
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> List("", "https")
      }
    }
  }

}
