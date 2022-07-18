package io.frontroute

import io.frontroute.testing._
import utest._
import scala.scalajs.js
import scala.scalajs.js.JSON

object PathTests extends TestBase {

  val tests: Tests = Tests {

    test("simple pathEnd") {
      println("simple pathEnd")
      routeTest(
        route = probe =>
          pathEnd {
            testComplete {
              probe.append("end")
            }
          },
        init = locationProvider => {
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> List("end")
      }
    }

    test("path with oneOf path matcher") {
      println("path with oneOf path matcher")
      routeTest(
        route = probe =>
          path(segment(Set("a", "b"))) { str =>
            testComplete {
              probe.append(str)
            }
          },
        init = locationProvider => {
          locationProvider.path("a")
          locationProvider.path("b")
        }
      ) { probe =>
        probe.toList ==> List("a", "b")
      }
    }

    test("path with oneOf path matcher and recover") {
      println("path with oneOf path matcher and recover")
      routeTest(
        route = probe =>
          path(segment(Set("a", "b")).recover("default")) { str =>
            testComplete {
              probe.append(str)
            }
          },
        init = locationProvider => {
          locationProvider.path("a")
          locationProvider.path("b")
          locationProvider.path("c")
        }
      ) { probe =>
        probe.toList ==> List("a", "b", "default")
      }
    }

//    test("revisit previous match") {
//      routeTest(
//        route = probe =>
//          concat(
//            pathEnd {
//              testComplete {
//                probe.append("end")
//              }
//            },
//            path("path1") {
//              testComplete {
//                probe.append("path1")
//              }
//            }
//          ),
//        init = locationProvider => {
//          locationProvider.path()
//          locationProvider.path("path1")
//          locationProvider.path()
//        }
//      ) { probe =>
//        probe.toList ==> List("end", "path1", "end")
//      }
//    }

//    test("test path prefix") {
//      routeTest(
//        route = probe =>
//          testPathPrefix("a" / "b") {
//            extractUnmatchedPath { unmatched =>
//              testComplete {
//                probe.append(unmatched.mkString("/"))
//              }
//            }
//          },
//        init = locationProvider => {
//          locationProvider.path("a", "b", "c", "d")
//        }
//      ) { probe =>
//        probe.toList ==> List("a/b/c/d")
//      }
//    }

//    test("test path") {
//      routeTest(
//        route = probe =>
//          testPath("a" / "b" / "c" / "d") {
//            extractUnmatchedPath { unmatched =>
//              testComplete {
//                probe.append(unmatched.mkString("/"))
//              }
//            }
//          },
//        init = locationProvider => {
//          locationProvider.path("a", "b", "c", "d")
//        }
//      ) { probe =>
//        probe.toList ==> List("a/b/c/d")
//      }
//    }

  }

}
