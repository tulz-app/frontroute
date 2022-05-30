package io.frontroute

import io.frontroute.testing._
import utest._
import scala.scalajs.js
import scala.scalajs.js.JSON

object ConcatTests extends TestBase {

  val tests: Tests = Tests {

    test("alternate path") {
      routeTest(
        route = probe =>
          concat(
            path("a") {
              testComplete {
                probe.append("a")
              }
            },
            path("b") {
              testComplete {
                probe.append("b")
              }
            },
            path("c") {
              testComplete {
                probe.append("c")
              }
            }
          ),
        init = locationProvider => {
          locationProvider.path("b")
          locationProvider.path("c")
          locationProvider.path("a")
        }
      ) { probe =>
        probe.toList ==> List("b", "c", "a")
      }
    }

    test("deep alternate path") {
      routeTest(
        route = probe =>
          concat(
            pathPrefix("prefix1") {
              pathPrefix("prefix2") {
                concat(
                  pathEnd {
                    testComplete {
                      probe.append("prefix1/prefix2")
                    }
                  },
                  path("suffix1") {
                    testComplete {
                      probe.append("prefix1/prefix2/suffix1")
                    }
                  }
                )
              }
            },
            pathPrefix("prefix2") {
              pathPrefix("prefix3") {
                concat(
                  pathEnd {
                    testComplete {
                      probe.append("prefix2/prefix3")
                    }
                  },
                  path("suffix2") {
                    testComplete {
                      probe.append("prefix2/prefix3/suffix2")
                    }
                  },
                  path("suffix3") {
                    param("param1") { paramValue =>
                      testComplete {
                        probe.append(s"prefix2/prefix3/suffix3?param1=$paramValue")
                      }
                    }
                  }
                )
              }
            }
          ),
        init = locationProvider => {
          locationProvider.path("prefix2", "prefix3", "suffix2")
          locationProvider.path("prefix1", "prefix2")
          locationProvider.path("prefix1", "prefix2", "suffix1")
          locationProvider.path("prefix2", "prefix3")
          locationProvider.path("prefix2", "prefix3", "suffix3")
          locationProvider.params("param1" -> "param-value")
        }
      ) { probe =>
        probe.toList ==> List(
          "prefix2/prefix3/suffix2",
          "prefix1/prefix2",
          "prefix1/prefix2/suffix1",
          "prefix2/prefix3",
          "prefix2/prefix3/suffix3?param1=param-value"
        )
      }
    }

  }

}
