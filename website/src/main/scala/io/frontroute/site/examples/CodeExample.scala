package io.frontroute.site.examples

import com.raquo.laminar.api.L._
import io.laminext.AmAny
import io.laminext.AmendedHtmlTag
import org.scalajs.dom

abstract class CodeExample(val id: String, val title: String, val description: String)(
  _code: sourcecode.Text[AmendedHtmlTag[dom.html.Anchor, AmAny] => Element]
) {

  val code: sourcecode.Text[AmendedHtmlTag[dom.html.Anchor, AmAny] => Element] = _code

}
