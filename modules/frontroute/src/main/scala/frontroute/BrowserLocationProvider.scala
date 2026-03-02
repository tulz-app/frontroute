package frontroute

import com.raquo.laminar.api.L.*
import org.scalajs.dom

import scala.scalajs.js

class BrowserLocationProvider(
  popStateEvents: EventStream[dom.PopStateEvent],
  val baseName: String,
) extends LocationProvider {

  private val currentVar: Var[Option[Either[Unit, Location]]] = Var(Option.empty)

  // left - baseName did not match
  val current: Signal[Option[Either[Unit, Location]]] = currentVar.signal.distinct

  def start()(implicit owner: Owner): Subscription = {
    EventStream
      .merge(
        EventStream.fromValue(js.undefined: js.Any),
        popStateEvents.map(_.state),
      )
      .map(state => Location(dom.window.location, state, baseName))
      .foreach {
        case Some(l) =>
          currentVar.set(Some(Right(l)))
        case None    =>
          currentVar.set(Some(Left(())))
      }
  }

}
