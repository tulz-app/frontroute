package frontroute

import frontroute.testing._

import scala.scalajs.js
import scala.scalajs.js.JSON

class HistoryStateTest extends TestBase {

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
      probe.toList shouldBe List(""""NO-STATE"""", """{"a":"test"}""", """{"a":"test","b":"something"}""")
    }
  }

}
