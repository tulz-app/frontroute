package io.frontroute

import io.frontroute.testing._
import utest._

import scala.scalajs.js
import scala.scalajs.js.JSON

object MiscTests extends TestBase {

  val tests: Tests = Tests {

    test("extractHostname") {
      routeTest(
        route = probe =>
          extractHostname { hostname =>
            complete {
              probe.append(hostname)
            }
          },
        init = locationProvider => {
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> List("test.nowhere")
      }
    }

    test("extractPort") {
      routeTest(
        route = probe =>
          extractPort { port =>
            complete {
              probe.append(port)
            }
          },
        init = locationProvider => {
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> List("443")
      }
    }

    test("extractHost") {
      routeTest(
        route = probe =>
          extractHost { host =>
            complete {
              probe.append(host)
            }
          },
        init = locationProvider => {
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> List("test.nowhere:443")
      }
    }

    test("extractProtocol") {
      routeTest(
        route = probe =>
          extractProtocol { protocol =>
            complete {
              probe.append(protocol)
            }
          },
        init = locationProvider => {
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> List("https")
      }
    }

    test("extractOrigin") {
      routeTest(
        route = probe =>
          extractOrigin { origin =>
            complete {
              probe.append(origin.getOrElse("---"))
            }
          },
        init = locationProvider => {
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> List("https://test.nowhere:443")
      }
    }

    test("historyState") {
      routeTest(
        route = probe =>
          historyState { state =>
            complete {
              probe.append(JSON.stringify(state.getOrElse("NO-STATE")))
            }
          },
        init = locationProvider => {
          locationProvider.path("state-test")
          locationProvider.state(js.Dynamic.literal(a = "test"))
          locationProvider.state(js.Dynamic.literal(a = "test", b = "something"))
        }
      ) { probe =>
        probe.toList ==> List(""""NO-STATE"""", """{"a":"test"}""", """{"a":"test","b":"something"}""")
      }
    }

    test("state") {
      var counter = 0
      routeTest(
        route = probe =>
          state {
            counter = counter + 1
            counter
          } { state =>
            extractUnmatchedPath { _ =>
              complete {
                probe.append(state.toString)
              }
            }
          },
        init = locationProvider => {
          locationProvider.path("state-test-1")
          locationProvider.path("state-test-2")
          locationProvider.path("state-test-3")
          locationProvider.path("state-test-4")
        }
      ) { probe =>
        probe.toList ==> List("1", "1", "1", "1")
      }
    }

  }

}
