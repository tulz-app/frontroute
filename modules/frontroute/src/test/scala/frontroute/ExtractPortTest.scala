package frontroute

import frontroute.testing.*
import org.scalatest.OptionValues

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
      probe.toList should have size 1
      // TODO: in tests, port is parsed as empty, even when explicitly specified
      probe.toList.headOption.value should (equal("").or(equal("443")))
    }
  }

}
