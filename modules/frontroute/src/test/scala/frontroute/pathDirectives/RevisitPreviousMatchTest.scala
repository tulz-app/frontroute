package frontroute.pathDirectives

import frontroute.testing.TestBase
import frontroute._

class RevisitPreviousMatchTest extends TestBase {

  test("revisit previous match") {
    routeTest(
      route = probe =>
        firstMatch(
          pathEnd {
            testComplete {
              probe.append("end")
            }
          },
          path("path1") {
            testComplete {
              probe.append("path1")
            }
          }
        ),
      init = locationProvider => {
        locationProvider.path()
        locationProvider.path("path1")
        locationProvider.path()
      }
    ) { probe =>
      probe.toList shouldBe List("end", "path1", "end")
    }
  }

}
