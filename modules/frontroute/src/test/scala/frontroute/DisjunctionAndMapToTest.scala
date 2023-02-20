package frontroute

import frontroute.testing._

class DisjunctionAndMapToTest extends TestBase {

  test("disjunction and .mapTo") {
    case class Page(isIndex: Boolean)
    routeTest(
      route = probe =>
        (pathEnd.mapTo(true) | path("page-1").mapTo(false)) { isIndex =>
          testComplete {
            probe.append(Page(isIndex).toString)
          }
        },
      init = locationProvider => {
        locationProvider.path()
        locationProvider.path("page-1")
        locationProvider.path()
        locationProvider.path("page-1")
        locationProvider.path()
      }
    ) { probe =>
      probe.toList shouldBe Seq(
        Page(true).toString,
        Page(false).toString,
        Page(true).toString,
        Page(false).toString,
        Page(true).toString
      )
    }
  }

}
