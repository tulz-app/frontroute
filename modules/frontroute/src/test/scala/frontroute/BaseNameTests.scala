package frontroute

import frontroute.testing.*

class BaseNameTests extends TestBase {

  test("alternate path") {
    routeTest(
      route = probe =>
        firstMatch(
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
        locationProvider.path("test-base-name", "b")
        locationProvider.path("test-base-name", "c")
        locationProvider.path("another-test-base-name")
        locationProvider.path("test-base-name", "a")
      },
      baseName = "/test-base-name"
    ) { probe =>
      probe.toList shouldBe List("b", "c", "a")
    }
  }

  test("deep alternate path") {
    routeTest(
      route = probe =>
        firstMatch(
          pathPrefix("prefix1") {
            pathPrefix("prefix2") {
              firstMatch(
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
              firstMatch(
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
        locationProvider.path("test-base-name", "prefix2", "prefix3", "suffix2")
        locationProvider.path("test-base-name", "prefix1", "prefix2")
        locationProvider.path("another-test-base-name")
        locationProvider.path("test-base-name", "prefix1", "prefix2", "suffix1")
        locationProvider.path("test-base-name", "prefix2", "prefix3")
        locationProvider.path("another-test-base-name")
        locationProvider.path("test-base-name", "prefix2", "prefix3", "suffix3")
        locationProvider.params("param1" -> "param-value")
      },
      baseName = "/test-base-name"
    ) { probe =>
      probe.toList shouldBe List(
        "prefix2/prefix3/suffix2",
        "prefix1/prefix2",
        "prefix1/prefix2/suffix1",
        "prefix2/prefix3",
        "prefix2/prefix3/suffix3?param1=param-value"
      )
    }
  }

}
