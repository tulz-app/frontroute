package frontroute

import com.raquo.laminar.api.L._
import org.scalajs.dom

import scala.scalajs.js

class BrowserLocationProvider(
  popStateEvents: EventStream[dom.PopStateEvent]
) extends LocationProvider {

  private val currentVar                = Var(Option.empty[Location])
  val current: Signal[Option[Location]] = currentVar.signal

  def start()(implicit owner: Owner): Subscription = {
    EventStream
      .merge(
        EventStream.fromValue(js.undefined: js.Any),
        popStateEvents.map(_.state),
      )
      .map(state => Location(dom.window.location, state))
      .foreach { l =>
        currentVar.set(Some(l))
      }
  }

}
