package io.frontroute

import io.frontroute.testing._
import utest._

object Disjunction2xAndMapTest extends TestBase {

  val tests: Tests = Tests {

    test("2x disjunction and .map") {
      case class Page(index: Int)
      routeTest(
        route = probe =>
          (
            pathEnd.map(_ => 0) |
              path("page-1").map(_ => 1) |
              path("page-2").map(_ => 2)
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
        probe.toList ==> Seq(
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

}
