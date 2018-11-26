package app.tulz.routing

import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.eventstream.EventStream
import com.raquo.airstream.ownership.Owner
import com.raquo.airstream.signal.Signal
import org.scalajs.dom
import org.scalajs.dom.PopStateEvent
import org.scalajs.dom.raw.Location

import scala.scalajs.js

case class Loc(
  path: List[String],
  params: Map[String, Seq[String]]
)

object Loc {

  val empty = Loc(List.empty, Map.empty)

  def apply(location: Location): Loc =
    Loc(
      path = location.pathname.dropWhile(_ == '/').split('/').toList.dropWhile(_.isEmpty),
      params = ParamsParser.extractParams(location.search)
    )

  private def fromEvent[E](target: dom.EventTarget, event: String): EventStream[E] = {
    val bus = new EventBus[E]
    val eventHandler: js.Function1[E, Unit] = (e: E) => {
      bus.writer.onNext(e)
    }
    target.addEventListener(event, eventHandler)
    bus.events
  }

  def locations(implicit owner: Owner): Signal[Loc] =
    fromEvent[PopStateEvent](dom.window, "popstate")
      .map(_ => dom.window.location)
      .map(Loc(_))
      .toSignal(Loc(dom.window.location))

}
