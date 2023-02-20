package frontroute

import frontroute.testing._

class ConjunctionTests extends TestBase {

  test("conjunction") {
    routeTest(
      route = probe =>
        pathPrefix("prefix1") {
          pathPrefix("prefix2") {
            (path(segment) & param("param1")) { (seg, paramValue) =>
              testComplete {
                probe.append(s"prefix1/prefix2/$seg?param1=$paramValue")
              }
            }
          }
        },
      init = locationProvider => {
        locationProvider.path("prefix1", "prefix2", "other-suffix-1")
        locationProvider.params("param1" -> "param1-value1")
        locationProvider.params("param1" -> "param1-value2")
        locationProvider.params("param1" -> "param1-value2") // dup
        locationProvider.path("prefix1", "prefix2", "other-suffix-2")
      }
    ) { probe =>
      probe.toList shouldBe List(
        "prefix1/prefix2/other-suffix-1?param1=param1-value1",
        "prefix1/prefix2/other-suffix-1?param1=param1-value2",
        "prefix1/prefix2/other-suffix-2?param1=param1-value2"
      )
    }
  }

}
