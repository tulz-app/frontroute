package frontroute.site.layout

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement

object PageFooter {

  def apply(
  ): ReactiveHtmlElement.Base =
    div(
      cls := "hidden bg-gray-900 text-white p-4"
    )

}
