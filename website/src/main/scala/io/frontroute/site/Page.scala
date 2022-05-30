package io.frontroute.site

import com.raquo.laminar.api.L._

final class Page private (
  val path: String,
  val title: String,
  val render: () => Element
)

object Page {

  def apply(
    path: String,
    title: String,
    render: => Element
  ): Page = new Page(
    path,
    title,
    () => render
  )

}
