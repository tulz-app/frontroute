package app.tulz.routing

import com.raquo.airstream.core.Observer
import com.raquo.airstream.ownership.Owner
import com.raquo.airstream.signal.{Signal, Val, Var}
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js.timers._

object RoutingTests extends TestSuite {

  def singleLoc(path: String*): Signal[Loc] =
    Val(
      loc(path: _*)
    )

  def loc(path: String*): Loc =
    Loc(path = path.toList, params = Map.empty)

  case class Context()
  case class Page(p: String)
  case class PageWithSignal($segment: Signal[String])

  implicit val owner: Owner = new Owner {}
  val $context              = Val(Context())
  val routing               = new Routing[Context]
  import routing._

  val tests = Tests {

    "pathEnd works" - {
      val route = pathEnd.map(_ => "end").mapTo(Page("end"))
      * - {
        val $pages = runRoute(route, singleLoc(), $context)
        nSignals(1, $pages).map { p =>
          p ==> Some(Page("end")) :: Nil
        }
      }
    }

    "sub signal works" - {
      val route = pathPrefix("prefix").sub { _ =>
        path(segment).signal.map { $segment =>
          PageWithSignal($segment)
        }
      }
      * - {
        val $locs = generateSignals(
          List(
            loc("prefix", "1"),
            loc("prefix", "2"),
            loc("prefix", "3"),
            loc("prefix", "2"),
            loc("prefix2", "2"),
            loc("prefix", "5"),
            loc("prefix", "6"),
            loc("prefix", "7"),
            loc("prefix", "6")
          )
        )
        val $pages = runRoute(route, $locs, $context)
        nthSignal(3, $pages).flatMap {
          case Some(page) =>
            nSignals(4, page.$segment).map { subLocs =>
              subLocs ==> List(
                "5",
                "6",
                "7",
                "6"
              )
            }
          case None => Future.failed(new RuntimeException("no prefix match"))
        }
      }
    }

  }

  def nthSignal[T](n: Int, s: Signal[T], waitTime: Long = 1000): Future[T] = {
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

  def nSignals[T](n: Int, s: Signal[T], waitTime: Long = 1000): Future[List[T]] = {
    val p     = Promise[List[T]]()
    var count = n
    var list  = List.empty[T]
    s.addObserver(Observer { t =>
      if (count >= 0) {
        count = count - 1
        list = t :: list
        if (count == 0) {
          p.success(list.reverse)
        }
      }
    })

    setTimeout(waitTime) {
      if (!p.isCompleted) {
        p.failure(new RuntimeException("nSignals timeout"))
      }
    }
    p.future
  }

  def generateSignals[T](s: List[T], interval: Long = 10): Signal[T] = s match {
    case head :: rest =>
      val $var = Var(head)
      var ss   = rest
      def doNext(): Unit = ss match {
        case h :: tail =>
          ss = tail
          $var.writer.onNext(h)
          setTimeout(interval) {
            doNext()
          }
        case _ =>
      }
      setTimeout(interval) {
        doNext()
      }
      $var.signal
    case _ => ???
  }

}
