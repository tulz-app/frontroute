package frontroute

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

package object testing {

  def delayedFuture(duration: FiniteDuration): Future[Unit] =
    Future.unit.delayed(duration)

  implicit class FutureExt[T](f: => Future[T]) {

    def delayed(duration: FiniteDuration): Future[T] = {
      val promise = Promise[T]()
      f.onComplete { t =>
        js.timers.setTimeout(duration) {
          promise.complete(t)
        }
      }
      promise.future
    }
  }

}
