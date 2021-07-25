package io.frontroute

import com.raquo.airstream.core.EventStream
import io.frontroute.testing._
import utest._

object MemoizeTests extends TestBase {

  val tests: Tests = Tests {

    test("memoize") {
      var asyncInvocations = List.empty[String]
      routeTest(
        route = probe =>
          path(segment) { value =>
            memoize(() => {
              asyncInvocations = asyncInvocations :+ value
              EventStream.fromValue(s"retrieved: $value")
            }) { retrieved =>
              complete {
                probe.append(retrieved)
              }
            }
          },
        init = locationProvider => {
          locationProvider.path("cat")
          locationProvider.path("dog")
          locationProvider.path("lizard")
          locationProvider.path("cat")
          locationProvider.path("dog")
          locationProvider.path("lizard")
        }
      ) { probe =>
        asyncInvocations ==> List(
          "cat",
          "dog",
          "lizard"
        )
        probe.toList ==> List(
          "retrieved: cat",
          "retrieved: dog",
          "retrieved: lizard",
          "retrieved: cat",
          "retrieved: dog",
          "retrieved: lizard"
        )
      }
    }

  }

}
