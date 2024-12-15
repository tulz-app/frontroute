package frontroute

import frontroute.testing.*

class ExtractHostNameTest extends TestBase {

  test("extractHostname") {
    routeTest(
      route = probe =>
        extractHostname { hostname =>
          testComplete {
            probe.append(hostname)
          }
        },
      init = locationProvider => {
        locationProvider.path()
      }
    ) { probe =>
      probe.toList shouldBe List("test.nowhere")
    }
  }

}
