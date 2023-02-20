package frontroute

import com.raquo.airstream.core.Signal
import frontroute.testing._
import scala.concurrent.Future

class Disjunction3xSignalTest extends TestBase {

  test("disjunction 3x signal") {
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
              testComplete {
                pathSignal = s
                signals = nSignals(5, pathSignal)
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
          suffixes shouldBe List(
            "default",
            "suffix-1",
            "suffix-1",
            "suffix-2",
            "suffix-3"
          )
          probe.toList shouldBe List(
            "prefix1/prefix2"
          )
        }
    }
  }

}
