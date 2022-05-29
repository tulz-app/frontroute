package io.frontroute

import scala.scalajs.js

class PageMeta private (
  val title: String,
  val description: Option[String],
  val keywords: Option[String],
  val robots: Option[String],
  val customMeta: Option[Map[String, String]],
  _status: String
) extends js.Object {

  def status: PageStatusCode = _status match {
    case "ok"        => PageStatusCode.Ok
    case "not-found" => PageStatusCode.NotFound
    case "error"     => PageStatusCode.Error
    case _           => PageStatusCode.Error
  }

}

object PageMeta {

  val empty: PageMeta = PageMeta("")

  def apply(
    title: String,
    description: Option[String] = None,
    keywords: Option[String] = None,
    robots: Option[String] = None,
    customMeta: Option[Map[String, String]] = None,
    status: PageStatusCode = PageStatusCode.Ok
  ): PageMeta = new PageMeta(
    title = title,
    description = description,
    keywords = keywords,
    robots = robots,
    customMeta = customMeta,
    _status = status match {
      case PageStatusCode.Ok       => "ok"
      case PageStatusCode.NotFound => "not-found"
      case PageStatusCode.Error    => "error"
    }
  )

}
