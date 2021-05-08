package io.frontroute.site.pages

import io.frontroute.site.components.CodeExampleDisplay
import io.frontroute.site.examples.CodeExample

object CodeExamplePage {

  def apply(example: CodeExample): PageRender = page(example.title) { () =>
    CodeExampleDisplay(example)
  }

}
