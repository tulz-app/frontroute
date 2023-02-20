package frontroute

import com.raquo.airstream.core.Signal
import frontroute.testing._

import scala.concurrent.Future

class SignalX3Test extends TestBase {

  test("signal x3") {
    var pathSignal: Signal[String]    = null
    var signals: Future[List[String]] = null
    routeTestF(
      route = probe =>
        pathPrefix("prefix1") {
          pathPrefix("prefix2") {
            path(segment).signal { s =>
              testComplete {
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
          params shouldBe List(
            "other-suffix-1",
            "other-suffix-2",
            "other-suffix-3"
          )
          probe.toList shouldBe List(
            "prefix1/prefix2"
          )
        }
    }
  }

}
