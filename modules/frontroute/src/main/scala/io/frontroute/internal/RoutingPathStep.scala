package io.frontroute.internal

sealed abstract class RoutingPathStep(repr: String) extends Product with Serializable {
  override def toString: String = repr
}

object RoutingPathStep {

  case object Initial           extends RoutingPathStep("i")
  case object Dir               extends RoutingPathStep("d")
  case class Concat(index: Int) extends RoutingPathStep(index.toString)
  case object Disjunction       extends RoutingPathStep("j")
  case object EnterDisjunction  extends RoutingPathStep("e")
  case object Conjunction       extends RoutingPathStep("c")

}
