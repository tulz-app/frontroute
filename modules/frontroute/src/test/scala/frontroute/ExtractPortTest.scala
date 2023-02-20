package frontroute

import frontroute.testing._

import scala.scalajs.js
import scala.scalajs.js.JSON

class ExtractPortTest extends TestBase {

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
      probe.toList shouldBe List("443")
    }
  }

}
