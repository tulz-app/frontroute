package frontroute.testing

import com.raquo.airstream.core.Observer
import com.raquo.airstream.core.Signal
import com.raquo.airstream.ownership.Owner
import com.raquo.airstream.state.Var
import com.raquo.domtestutils.scalatest.AsyncMountSpec
import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveElement
import com.raquo.laminar.nodes.RootNode
import com.raquo.laminar.utils.LaminarSpec
import frontroute.*
import org.scalajs.dom
import org.scalatest.BeforeAndAfterEach
import org.scalatest.OptionValues
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.*
import scala.scalajs.js.timers.setTimeout

abstract class TestBase extends AsyncFunSuite with Matchers with LaminarSpec with AsyncMountSpec with BeforeAndAfterEach with OptionValues {

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
    init: TestLocationProvider => Unit,
    baseName: BaseName = ""
  )(checks: Probe[String] => Future[T]): Future[T] = {
    if (baseName != "" && !(baseName.startsWith("/") && !baseName.endsWith("/")))
      throw new IllegalArgumentException("baseName must be empty; or start with /, and NOT end with /")

    val lp    = new TestLocationProvider(baseName)
    val probe = new Probe[String]

    mount(
      div(
        initRouting(lp),
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
    init: TestLocationProvider => Unit,
    baseName: BaseName = ""
  )(checks: Probe[String] => T): Future[T] = routeTestF[T](route, wait, init, baseName)(probe => Future.successful(checks(probe)))

  protected def routeTestDomF[T](
    route: => Route,
    wait: FiniteDuration = 250.millis,
    init: TestLocationProvider => Unit,
    baseName: BaseName = ""
  )(checks: dom.HTMLElement => Future[T]): Future[T] = {
    if (baseName != "" && !(baseName.startsWith("/") && !baseName.endsWith("/")))
      throw new IllegalArgumentException("baseName must be empty; or start with /, and NOT end with /")

    val lp = new TestLocationProvider(baseName)

    mount(
      div(
        initRouting(lp),
        route
      )
    )

    val future = delayedFuture(wait).flatMap { _ =>
      checks(root.container.asInstanceOf[dom.HTMLElement])
    }
    init(lp)
    future
  }

  protected def routeTestDom[T](
    route: => Route,
    wait: FiniteDuration = 10.millis,
    init: TestLocationProvider => Unit,
    baseName: BaseName = ""
  )(checks: dom.HTMLElement => T): Future[T] = routeTestDomF[T](route, wait, init, baseName)(root => Future.successful(checks(root)))

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
