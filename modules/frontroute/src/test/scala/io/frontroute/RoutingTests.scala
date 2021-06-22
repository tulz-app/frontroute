package io.frontroute

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.core.Observer
import com.raquo.airstream.core.Signal
import com.raquo.airstream.eventbus.EventBus
import io.frontroute.testing._
import com.raquo.airstream.ownership.Owner
import com.raquo.airstream.state.Var
import io.frontroute.internal.HistoryState
import utest._

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.scalajs.js.timers._
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.scalajs.js
import scala.scalajs.js.JSON

object RoutingTests extends TestSuite {

  implicit val testOwner: Owner = new Owner {}

  case class Page(p: String)

  case class PageWithSignal(segment: Signal[String])

  class Probe[A] {
    private val buffer = new ListBuffer[A]()

    def append(s: A): Unit = {
      val _ = buffer.append(s)
    }

    def toList: Seq[A] = buffer.toList
  }

  private def routeTestF[T](
    route: Probe[String] => Route,
    wait: FiniteDuration = 10.millis,
    init: TestLocationProvider => Unit
  )(checks: Probe[String] => Future[T]): Future[T] = {
    val locationProvider = new TestLocationProvider()
    val probe            = new Probe[String]

    val sub = runRoute(route(probe), locationProvider)(testOwner)
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
    route: Probe[String] => Route,
    wait: FiniteDuration = 10.millis,
    init: TestLocationProvider => Unit
  )(checks: Probe[String] => T): Future[T] = routeTestF[T](route, wait, init)(probe => Future.successful(checks(probe)))

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

    test("revisit previous match") {
      routeTest(
        route = probe =>
          concat(
            pathEnd {
              complete {
                probe.append("end")
              }
            },
            path("path1") {
              complete {
                probe.append("path1")
              }
            }
          ),
        init = locationProvider => {
          locationProvider.path()
          locationProvider.path("path1")
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> List("end", "path1", "end")
      }
    }

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
              addDirectiveApply(path(segment) & param("param1")).apply { (seg, paramValue) =>
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

    test("disjunction and .map") {
      case class Page(isIndex: Boolean)
      routeTest(
        route = probe =>
          (pathEnd.map(_ => true) | path("page-1").map(_ => false)) { isIndex =>
            complete {
              probe.append(Page(isIndex).toString)
            }
          },
        init = locationProvider => {
          locationProvider.path()
          locationProvider.path("page-1")
          locationProvider.path()
          locationProvider.path("page-1")
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> Seq(
          Page(true).toString,
          Page(false).toString,
          Page(true).toString,
          Page(false).toString,
          Page(true).toString
        )
      }
    }

    test("disjunction and .mapTo") {
      case class Page(isIndex: Boolean)
      routeTest(
        route = probe =>
          (pathEnd.mapTo(true) | path("page-1").mapTo(false)) { isIndex =>
            complete {
              probe.append(Page(isIndex).toString)
            }
          },
        init = locationProvider => {
          locationProvider.path()
          locationProvider.path("page-1")
          locationProvider.path()
          locationProvider.path("page-1")
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> Seq(
          Page(true).toString,
          Page(false).toString,
          Page(true).toString,
          Page(false).toString,
          Page(true).toString
        )
      }
    }

    test("disjunction and .collect") {
      case class Page(isIndex: Boolean)
      routeTest(
        route = probe =>
          (pathEnd.collect { case _ => true } | path("page-1").collect { case _ => false }) { isIndex =>
            complete {
              probe.append(Page(isIndex).toString)
            }
          },
        init = locationProvider => {
          locationProvider.path()
          locationProvider.path("page-1")
          locationProvider.path()
          locationProvider.path("page-1")
          locationProvider.path()
        }
      ) { probe =>
        probe.toList ==> Seq(
          Page(true).toString,
          Page(false).toString,
          Page(true).toString,
          Page(false).toString,
          Page(true).toString
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
              (path(segment) | pathEnd.map(_ => "default")).signal { s =>
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
    }(testOwner)

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

class TestLocationProvider extends LocationProvider {

  private var currentProtocol                          = "https"
  private var currentHostname                          = "test.nowhere"
  private var currentPort                              = "443"
  private var currentPath: List[String]                = List.empty
  private var currentParams: Map[String, List[String]] = Map.empty
  private var currentState: js.UndefOr[HistoryState]   = js.undefined

  private val bus = new EventBus[RouteLocation]

  val stream: EventStream[RouteLocation] = bus.events

  def protocol(protocol: String): Unit = {
    currentProtocol = protocol
    emit()
  }

  def hostname(hostname: String): Unit = {
    currentHostname = hostname
    emit()
  }

  def port(port: String): Unit = {
    currentPort = port
    emit()
  }

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

  def state(userState: js.UndefOr[js.Any]): Unit = {
    currentState = new HistoryState(internal = js.undefined, user = userState)
    emit()
  }

  def emit(): Unit = {
    bus.writer.onNext(
      RouteLocation(
        hostname = currentHostname,
        port = currentPort,
        protocol = currentProtocol,
        host = s"${currentHostname}:${currentPort}",
        origin = Some(s"${currentProtocol}://${currentHostname}:${currentPort}"),
        unmatchedPath = currentPath,
        params = currentParams,
        state = currentState
      )
    )
  }

}
