package io.frontroute.site.pages

import io.frontroute.site.components.DocumentationDisplay

object DocumentationPage {

  def apply(title: String, markdown: String): PageRender = page(title) { () =>
    DocumentationDisplay(title, markdown)
  }

}
