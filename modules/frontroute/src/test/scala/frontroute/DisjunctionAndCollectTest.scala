package frontroute

import frontroute.testing._

class DisjunctionAndCollectTest extends TestBase {

  test("disjunction and .collect") {
    case class Page(isIndex: Boolean)
    routeTest(
      route = probe =>
        (pathEnd.collect { case _ => true } | path("page-1").collect { case _ => false }) { isIndex =>
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
