package io.frontroute

import com.raquo.laminar.api.L._
import io.frontroute.testing._
import com.raquo.airstream.ownership.Owner
import utest._

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.scalajs.js.timers._
import directives._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

object RoutingTests extends TestSuite {

  implicit val testOwner: Owner = new Owner {}

  case class Page(p: String)
  case class PageWithSignal(segment: Signal[String])

  private def routeTestF[T](
    route: ListBuffer[String] => Route,
    wait: FiniteDuration = 10.millis,
    init: TestRouteLocationProvider => Unit = _ => {}
  )(checks: ListBuffer[String] => Future[T]): Future[T] = {
    val locationProvider = new TestRouteLocationProvider()
    val probe            = new ListBuffer[String]()

    val sub = runRoute(route(probe), locationProvider)(unsafeWindowOwner)
    val future = delayedFuture(wait).flatMap { _ =>
      try {
        checks(probe)
      } finally {
        sub.kill()
      }
    }
    init(locationProvider)
    future

  }

  private def routeTest[T](
    route: ListBuffer[String] => Route,
    wait: FiniteDuration = 10.millis,
    init: TestRouteLocationProvider => Unit = _ => {}
  )(checks: ListBuffer[String] => T): Future[T] = routeTestF[T](route, wait, init)(probe => Future.successful(checks(probe)))

  val tests: Tests = Tests {

    test("simple pathEnd") {
      routeTest(
        route = probe =>
          pathEnd {
            complete {
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

    test("alternate path") {
      routeTest(
        route = probe =>
          concat(
            path("a") {
              complete {
                probe.append("a")
              }
            },
            path("b") {
              complete {
                probe.append("b")
              }
            },
            path("c") {
              complete {
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
                    complete {
                      probe.append("prefix1/prefix2")
                    }
                  },
                  path("suffix1") {
                    complete {
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
                    complete {
                      probe.append("prefix2/prefix3")
                    }
                  },
                  path("suffix2") {
                    complete {
                      probe.append("prefix2/prefix3/suffix2")
                    }
                  },
                  path("suffix3") {
                    param("param1") { paramValue =>
                      complete {
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

    test("signal") {
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

    test("conjunction") {
      routeTest(
        route = probe =>
          pathPrefix("prefix1") {
            pathPrefix("prefix2") {
              (path(segment) & param("param1")) { (seg, paramValue) =>
                complete {
                  probe.append(s"prefix1/prefix2/$seg?param1=$paramValue")
                }
              }
            }
          },
        init = locationProvider => {
          locationProvider.path("prefix1", "prefix2", "other-suffix-1")
          locationProvider.params("param1" -> "param1-value1")
          locationProvider.params("param1" -> "param1-value2")
          locationProvider.path("prefix1", "prefix2", "other-suffix-2")

        }
      ) { probe =>
        probe.toList ==> List(
          "prefix1/prefix2/other-suffix-1?param1=param1-value1",
          "prefix1/prefix2/other-suffix-1?param1=param1-value2",
          "prefix1/prefix2/other-suffix-2?param1=param1-value2"
        )
      }
    }

    test("disjunction signal") {
      var pathSignal: Signal[String]    = null
      var signals: Future[List[String]] = null
      routeTestF(
        route = probe =>
          pathPrefix("prefix1") {
            pathPrefix("prefix2") {
              (path(segment) | pathEnd.tmap(_ => Tuple1("default"))).signal { s =>
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

  def nthSignal[T](n: Int, s: Signal[T], waitTime: FiniteDuration = 1.second): Future[T] = {
    val p     = Promise[T]()
    var count = n
    s.addObserver(Observer { t =>
      if (count >= 0) {
        count = count - 1
        if (count == 0) {
          p.success(t)
        }
      }
    })

    setTimeout(waitTime) {
      if (!p.isCompleted) {
        p.failure(new RuntimeException("nthSignal timeout"))
      }
    }
    p.future
  }

  def nSignals[T](n: Int, s: Signal[T], wait: FiniteDuration = 1.second): Future[List[T]] = {
    val p     = Promise[List[T]]()
    var count = n
    var list  = List.empty[T]
    s.foreach { t =>
      if (count >= 0) {
        count = count - 1
        list = t :: list
        if (count == 0) {
          p.success(list.reverse)
        }
      }
    }(unsafeWindowOwner)

    setTimeout(wait) {
      if (!p.isCompleted) {
        println(s"nSignals timeout, waited $wait for $n signals: $list")
        p.failure(new RuntimeException("nSignals timeout"))
      }
    }
    p.future
  }

  def generateSignals[T](s: List[T], interval: FiniteDuration = 10.millis): Signal[T] = {
    s match {
      case head :: rest =>
        val $var = Var(head)
        var ss   = rest
        def doNext(): Unit = ss match {
          case h :: tail =>
            ss = tail
            $var.writer.onNext(h)
            val _ = setTimeout(interval) {
              doNext()
            }
          case _ =>
        }
        val _ = setTimeout(interval) {
          doNext()
        }
        $var.signal
      case _ =>
        throw new RuntimeException("generate signals - empty")
    }
  }

}

class TestRouteLocationProvider extends RouteLocationProvider {

  private var currentPath: List[String]                = List.empty
  private var currentParams: Map[String, List[String]] = Map.empty

  private val bus = new EventBus[RouteLocation]

  val stream: EventStream[RouteLocation] = bus.events

  def path(parts: String*): Unit = {
    currentPath = parts.toList
    emit()
  }

  def params(params: (String, String)*): Unit = {
    currentParams = params
      .groupBy(_._1)
      .view
      .map { case (name, values) =>
        name -> values.map(_._2).toList
      }
      .toMap
    emit()
  }

  def emit(): Unit = {
    bus.writer.onNext(
      RouteLocation(currentPath, currentParams)
    )
  }

}
