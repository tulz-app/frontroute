package frontroute.pathDirectives

import frontroute._
import frontroute.testing.TestBase

class SimplePathEndTest extends TestBase {

  test("simple pathEnd") {
    routeTest(
      route = probe =>
        pathEnd {
          testComplete {
            probe.append("end")
          }
        },
      init = locationProvider => {
        locationProvider.path()
      }
    ) { probe =>
      probe.toList shouldBe List("end")
    }
  }

}
