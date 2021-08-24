package io.frontroute

import com.raquo.airstream.core.Signal
import io.frontroute.testing._
import utest._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

object SignalTests extends TestBase {

  val tests: Tests = Tests {

    test("signal x1") {
      var pathSignal: Signal[String]    = null
      var signals: Future[List[String]] = null
      routeTestF(
        route = probe =>
          pathPrefix("prefix1") {
            pathPrefix("prefix2") {
              path(segment).signal { s =>
                complete {
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

    test("signal x3") {
      var pathSignal: Signal[String]    = null
      var signals: Future[List[String]] = null
      routeTestF(
        route = probe =>
          pathPrefix("prefix1") {
            pathPrefix("prefix2") {
              path(segment).signal { s =>
                complete {
                  pathSignal = s
                  signals = nSignals(3, pathSignal)
                  probe.append("prefix1/prefix2")
                }
              }
            }
          },
        init = locationProvider => {
          locationProvider.path("prefix1", "prefix2", "other-suffix-1")
          locationProvider.path("prefix1", "prefix2", "other-suffix-2")
          locationProvider.path("prefix1", "prefix2", "other-suffix-3")
        }
      ) { probe =>
        signals
          .map { params =>
            params ==> List(
              "other-suffix-1",
              "other-suffix-2",
              "other-suffix-3"
            )
          }
          .map { _ =>
            probe.toList ==> List(
              "prefix1/prefix2"
            )
          }
      }
    }

    test("param signal") {
      var paramSignal: Signal[String]   = null
      var signals: Future[List[String]] = null
      routeTestF(
        route = probe =>
          pathPrefix("prefix1") {
            pathPrefix("prefix2") {
              param("test-param").signal { s =>
                complete {
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

    test("two maybeParams signal") {
      var paramSignal1: Signal[Option[String]]   = null
      var paramSignal2: Signal[Option[String]]   = null
      var signals1: Future[List[Option[String]]] = null
      var signals2: Future[List[Option[String]]] = null
      routeTestF(
        route = probe =>
          pathPrefix("prefix1") {
            pathPrefix("prefix2") {
              (maybeParam("test-param-1").signal & maybeParam("test-param-2").signal) { (p1, p2) =>
                complete {
                  paramSignal1 = p1
                  paramSignal2 = p2
                  signals1 = nSignals(4, paramSignal1)
                  signals2 = nSignals(4, paramSignal2)
                  probe.append("prefix1/prefix2")
                }
              }
            }
          },
        wait = 50.millis,
        init = locationProvider => {
          locationProvider.path("prefix1", "prefix2")
          locationProvider.params("test-param-1" -> "value-1-1", "test-param-2" -> "value-2-1")
          locationProvider.params("test-param-1" -> "value-1-2", "test-param-2" -> "value-2-2")
          locationProvider.params("test-param-1" -> "value-1-3", "test-param-2" -> "value-2-3")
        }
      ) { probe =>
        for {
          _ <- signals1
                 .zip(signals2)
                 .map { case (params1, params2) =>
                   (params1, params2) ==>
                     List(
                       None,
                       Some("value-1-1"),
                       Some("value-1-2"),
                       Some("value-1-3")
                     ) -> List(
                       None,
                       Some("value-2-1"),
                       Some("value-2-2"),
                       Some("value-2-3")
                     )
                 }
          _ = probe.toList ==> List(
                "prefix1/prefix2"
              )
        } yield ()
      }
    }

    test("disjunction signal") {
      var pathSignal: Signal[String]    = null
      var signals: Future[List[String]] = null
      routeTestF(
        route = probe =>
          pathPrefix("prefix1") {
            pathPrefix("prefix2") {
              (path(segment) | pathEnd.map(_ => "default")).signal { s =>
                complete {
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

    test("3x disjunction signal") {
      var pathSignal: Signal[String]    = null
      var signals: Future[List[String]] = null
      routeTestF(
        route = probe =>
          pathPrefix("prefix1") {
            pathPrefix("prefix2") {
              (
                path("suffix-1").mapTo("suffix-1") |
                  path("suffix-2").mapTo("suffix-2") |
                  path("suffix-3").mapTo("suffix-3") |
                  pathEnd.mapTo("default")
              ).signal { s =>
                complete {
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
