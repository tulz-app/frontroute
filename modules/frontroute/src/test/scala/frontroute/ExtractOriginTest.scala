package frontroute

import frontroute.testing.*
import org.scalatest.OptionValues

import scala.scalajs.js
import scala.scalajs.js.JSON

class ExtractOriginTest extends TestBase with OptionValues {

  test("extractOrigin") {
    routeTest(
      route = probe =>
        extractOrigin { origin =>
          testComplete {
            probe.append(origin)
          }
        },
      init = locationProvider => {
        locationProvider.path()
      }
    ) { probe =>
      probe.toList should have size 1
      // TODO: in tests, port is parsed as empty, even when explicitly specified
      probe.toList.headOption.value should (equal("https://test.nowhere:443").`or`(equal("https://test.nowhere")))
    }
  }

}
