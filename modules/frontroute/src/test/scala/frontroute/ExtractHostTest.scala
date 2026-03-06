package frontroute

import frontroute.testing.*
import org.scalatest.OptionValues

import scala.scalajs.js
import scala.scalajs.js.JSON

class ExtractHostTest extends TestBase with OptionValues {

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
      probe.toList should have size 1
      // TODO: in tests, port is parsed as empty, even when explicitly specified
      probe.toList.headOption.value should (equal("test.nowhere:443").`or`(equal("test.nowhere")))
    }
  }

}
