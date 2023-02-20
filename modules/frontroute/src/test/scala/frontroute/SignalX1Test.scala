package frontroute

import com.raquo.airstream.core.Signal
import frontroute.testing._

import scala.concurrent.Future

class SignalX1Test extends TestBase {

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
          params shouldBe List(
            "other-suffix-1"
          )
          probe.toList shouldBe List(
            "prefix1/prefix2"
          )
        }
    }
  }

}
