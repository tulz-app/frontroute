package frontroute

import app.tulz.tuplez.Composition

trait ConjunctionMagnet[L] {
  type Out
  def apply(underlying: Directive[L]): Out
}

object ConjunctionMagnet {

  implicit def fromDirective[L, R](other: Directive[R])(implicit composition: Composition[L, R]): ConjunctionMagnet[L] {
    type Out = Directive[composition.Composed]
  } =
    new ConjunctionMagnet[L] {
      type Out = Directive[composition.Composed]
      def apply(underlying: Directive[L]): Directive[composition.Composed] =
        Directive[composition.Composed] { inner => (location, previous, state) =>
          underlying.tapply { prefix => (location, previous, state) =>
            other.tapply { suffix =>
              inner(composition.compose(prefix, suffix))
            }(location, previous, state.enterConjunction)
          }(location, previous, state)
        }
    }

}
