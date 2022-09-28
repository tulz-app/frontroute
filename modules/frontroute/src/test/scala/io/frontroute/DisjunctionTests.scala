package io.frontroute

import com.raquo.airstream.core.Signal
import io.frontroute.testing._
import utest._
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON

object DisjunctionTests extends TestBase {

  val tests: Tests = Tests {

    test("disjunction and .map") {
      case class Page(isIndex: Boolean)
      routeTest(
        route = probe =>
          (pathEnd.map(_ => true) | path("page-1").map(_ => false)) { isIndex =>
            complete {
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

    test("2x disjunction and .map") {
      case class Page(index: Int)
      routeTest(
        route = probe =>
          (
            pathEnd.map(_ => 0) |
              path("page-1").map(_ => 1) |
              path("page-2").map(_ => 2)
          ) { index =>
            complete {
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

    test("disjunction and .mapTo") {
      case class Page(isIndex: Boolean)
      routeTest(
        route = probe =>
          (pathEnd.mapTo(true) | path("page-1").mapTo(false)) { isIndex =>
            complete {
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

    test("2x disjunction and .mapTo") {
      case class Page(index: Int)
      routeTest(
        route = probe =>
          (
            pathEnd.mapTo(0) |
              path("page-1").mapTo(1) |
              path("page-2").mapTo(2)
          ) { index =>
            complete {
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

    test("disjunction and .collect") {
      case class Page(isIndex: Boolean)
      routeTest(
        route = probe =>
          (pathEnd.collect { case _ => true } | path("page-1").collect { case _ => false }) { isIndex =>
            complete {
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

    test("2x disjunction and .collect") {
      case class Page(index: Int)
      routeTest(
        route = probe =>
          (
            pathEnd.collect { case _ => 0 } |
              path("page-1").collect { case _ => 1 } |
              path("page-2").collect { case _ => 2 }
          ) { index =>
            complete {
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
