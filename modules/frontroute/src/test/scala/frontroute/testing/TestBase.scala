package frontroute.testing

import com.raquo.airstream.core.Observer
import com.raquo.airstream.core.Signal
import com.raquo.airstream.ownership.Owner
import com.raquo.airstream.state.Var
import com.raquo.domtestutils.scalatest.AsyncMountSpec
import com.raquo.laminar.api.L._
import com.raquo.laminar.utils.LaminarSpec
import frontroute._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.scalajs.js.timers.setTimeout

abstract class TestBase extends AsyncFunSuite with Matchers with LaminarSpec with AsyncMountSpec with BeforeAndAfterEach {

  implicit protected val testOwner: Owner = new Owner {}

  override protected def afterEach(): Unit = {
    if (root != null) {
      unmount()
    }
  }

  case class Page(p: String)

  case class PageWithSignal(segment: Signal[String])

  class Probe[A] {
    private val buffer = new ListBuffer[A]()

    def append(s: A): Unit = {
      val _ = buffer.append(s)
    }

    def toList: Seq[A] = buffer.toList
  }

  protected def testComplete(body: => Unit): HtmlElement = {
    val _ = body
    div()
  }

  protected def routeTestF[T](
    route: Probe[String] => Route,
    wait: FiniteDuration = 10.millis,
    init: TestLocationProvider => Unit
  )(checks: Probe[String] => Future[T]): Future[T] = {
    val lp    = new TestLocationProvider()
    val probe = new Probe[String]

    mount(
      div(
        locationProvider(lp),
        route(probe)
      )
    )

    val future = delayedFuture(wait).flatMap { _ =>
      checks(probe)
    }
    init(lp)
    future
  }

  protected def routeTest[T](
    route: Probe[String] => Route,
    wait: FiniteDuration = 10.millis,
    init: TestLocationProvider => Unit
  )(checks: Probe[String] => T): Future[T] = routeTestF[T](route, wait, init)(probe => Future.successful(checks(probe)))

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
        p.failure(new RuntimeException(s"nthSignal timeout: ${s}"))
      }
    }
    p.future
  }

  protected def nSignals[T](n: Int, s: Signal[T], wait: FiniteDuration = 1.second): Future[List[T]] = {
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
        p.failure(new RuntimeException(s"nSignals timeout: ${s}, waited $wait for $n signals: $list"))
      }
    }
    p.future
  }

  protected def generateSignals[T](s: List[T], interval: FiniteDuration = 10.millis): Signal[T] = {
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
          case _         =>
        }

        val _ = setTimeout(interval) {
          doNext()
        }
        $var.signal
      case _            =>
        throw new RuntimeException("generate signals - empty")
    }
  }

}
