package frontroute
package internal
import scala.annotation.tailrec

class RoutingPath private (
  val path: List[RoutingPathStep]
) {

  def key: RoutingPath.Key = mkKey(path)

  private def mkKey(path: List[RoutingPathStep]): RoutingPath.Key = path.mkString

  def enter(): RoutingPath = new RoutingPath(path = RoutingPathStep.Dir :: path)

  def enterConcat(index: Int): RoutingPath = new RoutingPath(path = RoutingPathStep.Concat(index) :: path)

  def enterConjunction(): RoutingPath = new RoutingPath(path = RoutingPathStep.Conjunction :: path)

  def enterDisjunction(): RoutingPath = {
    new RoutingPath(path = RoutingPathStep.EnterDisjunction :: path)
  }

  def leaveDisjunction(): Option[(RoutingPath, List[RoutingPath.Key])] = {
    @tailrec
    def leave(
      path: List[RoutingPathStep],
      dropped: List[RoutingPath.Key]
    ): Option[(RoutingPath, List[RoutingPath.Key])] = {
      path match {
        case Nil => None

        case RoutingPathStep.EnterDisjunction :: tail =>
          val newPath = RoutingPathStep.Disjunction :: tail
          Some(
            (new RoutingPath(newPath), dropped)
          )

        case other => leave(other.tail, mkKey(other) :: dropped)

      }
    }

    leave(path, Nil)
  }

  override def toString: String = path.reverse.mkString(" ")

  override def equals(other: Any): Boolean = other match {
    case that: RoutingPath => path == that.path
    case _                 => false
  }

  override def hashCode(): Int = path.hashCode()

}

object RoutingPath {

  val initial = new RoutingPath(List(RoutingPathStep.Initial))
  val empty   = new RoutingPath(Nil)

  type Key = String

}
