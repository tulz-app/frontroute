package io.frontroute

case class DocumentMeta(
  title: String,
  description: Option[String] = None,
  keywords: Option[String] = None,
  robots: Option[String] = None,
  customMeta: Option[Map[String, String]] = None,
  status: PageStatusCode = PageStatusCode.Ok
)
