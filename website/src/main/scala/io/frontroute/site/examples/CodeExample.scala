package io.frontroute.site.examples

import com.raquo.laminar.api.L._
import io.frontroute.LocationProvider
import io.laminext.AmAny
import io.laminext.AmendedHtmlTag
import org.scalajs.dom

abstract class CodeExample(val id: String, val title: String, val description: String)(
  _code: sourcecode.Text[(LocationProvider, AmendedHtmlTag[dom.html.Anchor, AmAny]) => Element]
) {

  val code: sourcecode.Text[(LocationProvider, AmendedHtmlTag[dom.html.Anchor, AmAny]) => Element] = _code

}
