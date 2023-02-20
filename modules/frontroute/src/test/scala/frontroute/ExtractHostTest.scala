package frontroute

import frontroute.testing._

import scala.scalajs.js
import scala.scalajs.js.JSON

class ExtractHostTest extends TestBase {

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
      probe.toList shouldBe List("test.nowhere:443")
    }
  }

}
