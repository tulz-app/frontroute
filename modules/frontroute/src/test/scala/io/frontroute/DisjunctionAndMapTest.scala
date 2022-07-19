package io.frontroute

import io.frontroute.testing._
import utest._

object DisjunctionAndMapTest extends TestBase {

  val tests: Tests = Tests {

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
        probe.toList ==> Seq(
          Page(true).toString,
          Page(false).toString,
          Page(true).toString,
          Page(false).toString,
          Page(true).toString
        )
      }
    }

  }

}
