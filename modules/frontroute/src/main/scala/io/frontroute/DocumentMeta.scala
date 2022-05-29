package io.frontroute

case class DocumentMeta(
  title: String,
  description: Option[String],
  keywords: Option[String],
  robots: Option[String],
  customMeta: Option[Map[String, String]],
  status: PageStatusCode
)
