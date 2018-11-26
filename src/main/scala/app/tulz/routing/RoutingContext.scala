package app.tulz.routing

private class RoutingContext() {

  private var dataMap: Map[DirectiveId, Any]        = Map.empty
  private var currentDataMap: Map[DirectiveId, Any] = Map.empty

  def roll(): Unit = {
    dataMap = currentDataMap
    currentDataMap = Map.empty
  }

  def put[T](id: DirectiveId, data: T): Unit = {
    currentDataMap = currentDataMap + (id -> data)
  }

  def get[T](id: DirectiveId): Option[T] = {
    dataMap.get(id).map(_.asInstanceOf[T])
  }

}
