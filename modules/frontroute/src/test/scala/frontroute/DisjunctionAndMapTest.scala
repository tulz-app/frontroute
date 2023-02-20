package frontroute

import frontroute.testing._

class DisjunctionAndMapTest extends TestBase {

  test("disjunction and .map") {
    case class Page(isIndex: Boolean)
    routeTest(
      route = probe =>
        (pathEnd.map(_ => true) | path("page-1").map(_ => false)) { isIndex =>
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
