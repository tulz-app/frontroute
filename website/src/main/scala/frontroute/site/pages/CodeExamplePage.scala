package frontroute.site.pages

import com.raquo.laminar.api.L._
import frontroute.site.components.CodeExampleDisplay
import frontroute.site.examples.CodeExample

object CodeExamplePage {

  def apply(example: CodeExample): Element = page(example.title) {
    CodeExampleDisplay(example)
  }

}
