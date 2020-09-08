package app.tulz.routing

import app.tulz.util.Tuple
import app.tulz.util.TupleComposition.Composition

import scala.language.implicitConversions

trait ConjunctionMagnet[L] {
  type Out
  def apply(underlying: Directive[L]): Out
}

object ConjunctionMagnet {

  implicit def fromDirective[L, R](other: Directive[R])(implicit composition: Composition[L, R]): ConjunctionMagnet[L] { type Out = Directive[composition.C] } =
    new ConjunctionMagnet[L] {
      type Out = Directive[composition.C]
      def apply(underlying: Directive[L]): Directive[composition.C] =
        Directive[composition.C] { inner => (location, previous, state) =>
          underlying.tapply { prefix => (location, previous, state) =>
            other.tapply { suffix =>
              inner(composition.gc(prefix, suffix))
            }(location, previous, state.path("&"))
          }(location, previous, state)
        }(Tuple.yes)
    }

}
