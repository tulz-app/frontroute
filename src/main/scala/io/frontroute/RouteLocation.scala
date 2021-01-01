package io.frontroute

import io.frontroute.internal.HistoryState

final case class RouteLocation(
  hostname: String,
  port: String,
  protocol: String,
  host: String,
  origin: Option[String],
  unmatchedPath: List[String],
  params: Map[String, Seq[String]],
  state: Option[HistoryState]
) {

  @inline def withUnmatchedPath(path: List[String]): RouteLocation = this.copy(unmatchedPath = path)

  override def toString: String =
    s"${unmatchedPath.mkString("/")}${if (params.nonEmpty) "?" else ""}${params
      .flatMap { case (name, values) =>
        values.map(value => s"$name=$value")
      }
      .mkString("&")}"

}
