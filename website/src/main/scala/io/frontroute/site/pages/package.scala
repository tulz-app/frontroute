package io.frontroute.site

import com.raquo.laminar.api.L._
import io.frontroute.DocumentMeta
import io.frontroute.PageStatusCode
import io.frontroute.internal.Document

package object pages {

  def page(
    title: String,
    description: Option[String] = None,
    keywords: Option[String] = None,
    status: PageStatusCode = PageStatusCode.Ok
  )(
    content: => Element
  ): Element =
    content.amend(
      onMountCallback { _ =>
        Document.updateMeta(
          DocumentMeta(
            title = title,
            description = description,
            keywords = keywords,
            status = status
          )
        )
      }
    )

}
