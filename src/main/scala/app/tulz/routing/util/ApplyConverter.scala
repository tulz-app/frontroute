package app.tulz.routing.util
import app.tulz.routing.Route
import com.raquo.airstream.ownership.Owner

abstract class ApplyConverter[L] {
  type In
  def apply(f: In): L => Route
}

object ApplyConverter extends ApplyConverterInstances
