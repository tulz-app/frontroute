package frontroute

import com.raquo.laminar.api.L._
import com.raquo.laminar.api.L

case class DocumentMeta(
  title: String,
  description: Option[String] = None,
  keywords: Option[String] = None,
  robots: Option[String] = None,
  customMeta: Option[Map[String, String]] = None,
  status: PageStatusCode = PageStatusCode.Ok
)

object DocumentMeta {

  def set(
    pageMeta: DocumentMeta
  ): Unit = {
    set(
      pageMeta.title,
      pageMeta.description,
      pageMeta.keywords,
      pageMeta.robots,
      pageMeta.customMeta,
      pageMeta.status,
    )
  }

  def set(
    title: String,
    description: Option[String] = None,
    keywords: Option[String] = None,
    robots: Option[String] = None,
    customMeta: Option[Map[String, String]] = None,
    status: PageStatusCode = PageStatusCode.Ok
  ): Unit = {
    org.scalajs.dom.document.title = title
    var titleElement = org.scalajs.dom.document.head.querySelector("title")
    if (titleElement == null) {
      titleElement = titleTag(title).ref
      org.scalajs.dom.document.head.appendChild(titleElement)
    } else {
      titleElement.textContent = title
    }

    setMetaTag("description", description)
    setMetaTag("keywords", keywords)
    setMetaTag("robots", Option.when(status != PageStatusCode.Ok)("noindex").orElse(robots))
    customMeta.foreach { customMeta =>
      customMeta.foreach { case (name, value) =>
        setMetaTag(name, Option.when(value.nonEmpty)(value))
      }
    }
    setMetaTag("http-status", Option.when(status == PageStatusCode.NotFound)("404"))
  }

  private def setMetaTag(metaName: String, value: Option[String]): Unit = {
    var metaElement = org.scalajs.dom.document.head.querySelector(s"meta[name=${metaName}]")
    value match {
      case Some(value) =>
        if (metaElement == null) {
          metaElement = metaTag(nameAttr := metaName).ref
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
