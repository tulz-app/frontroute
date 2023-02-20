package frontroute.site.pages

import com.raquo.laminar.api.L._
import frontroute.site.components.DocumentationDisplay

object NotFoundPage {

  def apply(title: String, markdown: String): Element = page(title) {
    DocumentationDisplay(title, markdown)
  }

}
