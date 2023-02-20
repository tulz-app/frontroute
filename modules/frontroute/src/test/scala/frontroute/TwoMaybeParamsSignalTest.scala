package frontroute

import com.raquo.airstream.core.Signal
import frontroute.testing._

import scala.concurrent.Future
import scala.concurrent.duration._

class TwoMaybeParamsSignalTest extends TestBase {

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
              testComplete {
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
                 (params1, params2) shouldBe
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
      } yield probe.toList shouldBe List("prefix1/prefix2")
    }
  }

}
