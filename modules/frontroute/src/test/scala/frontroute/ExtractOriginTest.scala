package frontroute

import frontroute.testing._

import scala.scalajs.js
import scala.scalajs.js.JSON

class ExtractOriginTest extends TestBase {

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
      probe.toList shouldBe List("https://test.nowhere:443")
    }
  }

}
