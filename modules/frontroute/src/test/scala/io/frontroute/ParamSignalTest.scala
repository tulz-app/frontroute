package io.frontroute

import com.raquo.airstream.core.Signal
import io.frontroute.testing._
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global
import utest._

import scala.concurrent.Future
import scala.concurrent.duration._

object ParamSignalTest extends TestBase {

  val tests: Tests = Tests {

    test("param signal") {
      var paramSignal: Signal[String]   = null
      var signals: Future[List[String]] = null
      routeTestF(
        route = probe =>
          pathPrefix("prefix1") {
            pathPrefix("prefix2") {
              param("test-param").signal { s =>
                testComplete {
                  paramSignal = s
                  signals = nSignals(3, paramSignal)
                  probe.append("prefix1/prefix2")
                }
              }
            }
          },
        init = locationProvider => {
          locationProvider.path("prefix1", "prefix2")
          locationProvider.params("test-param" -> "value-1")
          locationProvider.params("test-param" -> "value-2")
          locationProvider.params("test-param" -> "value-3")
        }
      ) { probe =>
        signals
          .map { params =>
            params ==> List(
              "value-1",
              "value-2",
              "value-3"
            )
          }
          .map { _ =>
            probe.toList ==> List(
              "prefix1/prefix2"
            )
          }
      }
    }

  }

}
