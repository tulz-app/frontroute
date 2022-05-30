package io.frontroute.site.pages

import com.raquo.laminar.api.L._
import io.frontroute.site.components.CodeExampleDisplay
import io.frontroute.site.examples.CodeExample

object CodeExamplePage {

  def apply(example: CodeExample): Element = page(example.title) {
    CodeExampleDisplay(example)
  }

}
