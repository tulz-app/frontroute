package frontroute

import frontroute.testing._

class Disjunction2xAndMapToTest extends TestBase {

  test("2x disjunction and .mapTo") {
    case class Page(index: Int)
    routeTest(
      route = probe =>
        (
          pathEnd.mapTo(0) |
            path("page-1").mapTo(1) |
            path("page-2").mapTo(2)
        ) { index =>
          testComplete {
            probe.append(Page(index).toString)
          }
        },
      init = locationProvider => {
        locationProvider.path()
        locationProvider.path("page-1")
        locationProvider.path()
        locationProvider.path("page-1")
        locationProvider.path("page-2")
        locationProvider.path()
        locationProvider.path("page-2")
        locationProvider.path()
      }
    ) { probe =>
      probe.toList shouldBe Seq(
        Page(0).toString,
        Page(1).toString,
        Page(0).toString,
        Page(1).toString,
        Page(2).toString,
        Page(0).toString,
        Page(2).toString,
        Page(0).toString
      )
    }
  }

}
