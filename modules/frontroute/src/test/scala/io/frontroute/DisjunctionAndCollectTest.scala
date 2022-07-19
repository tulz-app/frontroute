package io.frontroute

import io.frontroute.testing._
import utest._

object DisjunctionAndCollectTest extends TestBase {

  val tests: Tests = Tests {

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
