package frontroute

import frontroute.testing._

import scala.scalajs.js
import scala.scalajs.js.JSON

class ExtractProtocolTest extends TestBase {

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
      probe.toList shouldBe List("https")
    }
  }

}
