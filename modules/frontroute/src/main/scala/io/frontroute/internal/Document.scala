package io.frontroute.internal

import com.raquo.laminar.api.L._
import io.frontroute.DocumentMeta
import io.frontroute.PageStatusCode

object Document {

  def update(
    pageMeta: DocumentMeta
  ): Unit = {
    org.scalajs.dom.document.title = pageMeta.title
    val titleElement = org.scalajs.dom.document.head.querySelector("title")
    titleElement.textContent = pageMeta.title

    setMetaTag("description", pageMeta.description)
    setMetaTag("keywords", pageMeta.keywords)
    setMetaTag("robots", Option.when(pageMeta.status != PageStatusCode.Ok)("noindex").orElse(pageMeta.robots))
    pageMeta.customMeta.foreach { customMeta =>
      customMeta.foreach { case (name, value) =>
        setMetaTag(name, Option.when(value.nonEmpty)(value))
      }
    }
  }

  private def setMetaTag(metaName: String, value: Option[String]): Unit = {
    var metaElement = org.scalajs.dom.document.head.querySelector(s"meta[name=${metaName}]'")
    value match {
      case Some(value) =>
        if (metaElement == null) {
          metaElement = meta(name := metaName).ref
          org.scalajs.dom.document.head.appendChild(metaElement)
        }
        metaElement.setAttribute("content", value)
      case None        =>
        if (metaElement != null) {
          val _ = org.scalajs.dom.document.head.removeChild(metaElement)
        }
    }
  }

}
