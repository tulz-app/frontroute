package io.frontroute

import com.raquo.airstream.core.Signal
import io.frontroute.testing._
import utest._
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON

object Disjunction2xAndCollectTests extends TestBase {

  val tests: Tests = Tests {

    test("2x disjunction and .collect") {
      case class Page(index: Int)
      routeTest(
        route = probe =>
          (
            pathEnd.collect { case _ => 0 } |
              path("page-1").collect { case _ => 1 } |
              path("page-2").collect { case _ => 2 }
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
