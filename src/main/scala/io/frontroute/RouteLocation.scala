package io.frontroute

final case class RouteLocation(
  unmatchedPath: List[String],
  params: Map[String, Seq[String]]
) {

  def withUnmatchedPath(path: List[String]): RouteLocation = this.copy(unmatchedPath = path)

  override def toString: String =
    s"${unmatchedPath.mkString("/")}${if (params.nonEmpty) "?" else ""}${params
      .flatMap { case (name, values) =>
        values.map(value => s"$name=$value")
      }
      .mkString("&")}"

}
