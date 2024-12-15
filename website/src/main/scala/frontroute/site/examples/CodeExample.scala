package frontroute.site.examples

import com.raquo.laminar.api.L.*

abstract class CodeExample(
  val id: String,
  val title: String,
  val description: String,
  val links: Seq[String]
)(
  _code: sourcecode.Text[() => Element]
) {

  val code: sourcecode.Text[() => Element] = _code

}
