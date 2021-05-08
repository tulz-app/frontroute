package io.frontroute.site

import io.frontroute.site.pages.PageRender

final case class Page(
  path: String,
  title: String,
  render: PageRender
)
