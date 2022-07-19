package io.frontroute

import com.raquo.airstream.core.Signal
import io.frontroute.testing._
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits.global
import utest._

import scala.concurrent.Future

object DisjunctionSignalTest extends TestBase {

  val tests: Tests = Tests {

    test("disjunction signal") {
      var pathSignal: Signal[String]    = null
      var signals: Future[List[String]] = null
      routeTestF(
        route = probe =>
          pathPrefix("prefix1") {
            pathPrefix("prefix2") {
              (path(segment) | pathEnd.map(_ => "default")).signal { s =>
                testComplete {
                  pathSignal = s
                  signals = nSignals(4, pathSignal)
                  probe.append("prefix1/prefix2")
                }
              }
            }
          },
        init = locationProvider => {
          locationProvider.path("prefix1", "prefix2")
          locationProvider.path("prefix1", "prefix2", "suffix-1")
          locationProvider.path("prefix1", "prefix2", "suffix-1")
          locationProvider.path("prefix1", "prefix2", "suffix-2")
          locationProvider.path("prefix1", "prefix2", "suffix-3")
        }
      ) { probe =>
        signals
          .map { suffixes =>
            suffixes ==> List(
              "default",
              "suffix-1",
              "suffix-2",
              "suffix-3"
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
