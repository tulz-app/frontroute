package io.frontroute

import com.raquo.airstream.core.Signal
import io.frontroute.testing._
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global
import utest._

import scala.concurrent.Future
import scala.concurrent.duration._

object SignalX1Test extends TestBase {

  val tests: Tests = Tests {

    test("signal x1") {
      var pathSignal: Signal[String]    = null
      var signals: Future[List[String]] = null
      routeTestF(
        route = probe =>
          pathPrefix("prefix1") {
            pathPrefix("prefix2") {
              path(segment).signal { s =>
                testComplete {
                  pathSignal = s
                  signals = nSignals(1, pathSignal)
                  probe.append("prefix1/prefix2")
                }
              }
            }
          },
        init = locationProvider => {
          locationProvider.path("prefix1", "prefix2", "other-suffix-1")
        }
      ) { probe =>
        signals
          .map { params =>
            params ==> List(
              "other-suffix-1"
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
