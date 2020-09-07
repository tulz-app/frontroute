package app.tulz.routing

final case class RouteLocation(
  unmatchedPath: List[String],
  params: Map[String, Seq[String]]
) {

  def withUnmatchedPath(path: List[String]): RouteLocation = this.copy(unmatchedPath = path)

}
