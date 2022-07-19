package io.frontroute

import io.frontroute.testing._
import utest._

import scala.scalajs.js
import scala.scalajs.js.JSON

object HistoryStateTest extends TestBase {

  val tests: Tests = Tests {

    test("historyState") {
      routeTest(
        route = probe =>
          historyState { state =>
            testComplete {
              probe.append(JSON.stringify(state.getOrElse("NO-STATE")))
            }
          },
        init = locationProvider => {
          locationProvider.path("state-test")
          locationProvider.state(js.Dynamic.literal(a = "test"))
          locationProvider.state(js.Dynamic.literal(a = "test", b = "something"))
        }
      ) { probe =>
        probe.toList ==> List(""""NO-STATE"""", """{"a":"test"}""", """{"a":"test","b":"something"}""")
      }
    }

  }

}
