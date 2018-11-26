package app.tulz.routing

object TupleComposition {

  trait Composition[-A, -B] {
    type C
    val gc: (A, B) => C
  }

  trait Composition_PriLowest {
    implicit def ***[A, B] = Composition[A, B, (A, B)]((_, _))
  }
  trait Composition_PriLow extends Composition_PriLowest {
    implicit def T16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P] = Composition[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O), P, (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)]((l, r) => (l._1, l._2, l._3, l._4, l._5, l._6, l._7, l._8, l._9, l._10, l._11, l._12, l._13, l._14, l._15, r))
    implicit def T15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O] = Composition[(A, B, C, D, E, F, G, H, I, J, K, L, M, N), O, (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)]((l, r) => (l._1, l._2, l._3, l._4, l._5, l._6, l._7, l._8, l._9, l._10, l._11, l._12, l._13, l._14, r))
    implicit def T14[A, B, C, D, E, F, G, H, I, J, K, L, M, N] = Composition[(A, B, C, D, E, F, G, H, I, J, K, L, M), N, (A, B, C, D, E, F, G, H, I, J, K, L, M, N)]((l, r) => (l._1, l._2, l._3, l._4, l._5, l._6, l._7, l._8, l._9, l._10, l._11, l._12, l._13, r))
    implicit def T13[A, B, C, D, E, F, G, H, I, J, K, L, M] = Composition[(A, B, C, D, E, F, G, H, I, J, K, L), M, (A, B, C, D, E, F, G, H, I, J, K, L, M)]((l, r) => (l._1, l._2, l._3, l._4, l._5, l._6, l._7, l._8, l._9, l._10, l._11, l._12, r))
    implicit def T12[A, B, C, D, E, F, G, H, I, J, K, L] = Composition[(A, B, C, D, E, F, G, H, I, J, K), L, (A, B, C, D, E, F, G, H, I, J, K, L)]((l, r) => (l._1, l._2, l._3, l._4, l._5, l._6, l._7, l._8, l._9, l._10, l._11, r))
    implicit def T11[A, B, C, D, E, F, G, H, I, J, K] = Composition[(A, B, C, D, E, F, G, H, I, J), K, (A, B, C, D, E, F, G, H, I, J, K)]((l, r) => (l._1, l._2, l._3, l._4, l._5, l._6, l._7, l._8, l._9, l._10, r))
    implicit def T10[A, B, C, D, E, F, G, H, I, J] = Composition[(A, B, C, D, E, F, G, H, I), J, (A, B, C, D, E, F, G, H, I, J)]((l, r) => (l._1, l._2, l._3, l._4, l._5, l._6, l._7, l._8, l._9, r))
    implicit def T9[A, B, C, D, E, F, G, H, I] = Composition[(A, B, C, D, E, F, G, H), I, (A, B, C, D, E, F, G, H, I)]((l, r) => (l._1, l._2, l._3, l._4, l._5, l._6, l._7, l._8, r))
    implicit def T8[A, B, C, D, E, F, G, H] = Composition[(A, B, C, D, E, F, G), H, (A, B, C, D, E, F, G, H)]((l, r) => (l._1, l._2, l._3, l._4, l._5, l._6, l._7, r))
    implicit def T7[A, B, C, D, E, F, G] = Composition[(A, B, C, D, E, F), G, (A, B, C, D, E, F, G)]((l, r) => (l._1, l._2, l._3, l._4, l._5, l._6, r))
    implicit def T6[A, B, C, D, E, F] = Composition[(A, B, C, D, E), F, (A, B, C, D, E, F)]((l, r) => (l._1, l._2, l._3, l._4, l._5, r))
    implicit def T5[A, B, C, D, E] = Composition[(A, B, C, D), E, (A, B, C, D, E)]((l, r) => (l._1, l._2, l._3, l._4, r))
    implicit def T4[A, B, C, D] = Composition[(A, B, C), D, (A, B, C, D)]((l, r) => (l._1, l._2, l._3, r))
    implicit def T3[A, B, C] = Composition[(A, B), C, (A, B, C)]((l, r) => (l._1, l._2, r))
  }
  trait Composition_PriMed extends Composition_PriLow {
    implicit def _toA[A] = Composition[Unit, A, A]((_, a) => a)
    implicit def Ato_[A] = Composition[A, Unit, A]((a, _) => a)
  }
  object Composition extends Composition_PriMed {
    implicit def _to_ = Composition[Unit, Unit, Unit]((_, _) => ())

    type Aux[A, B, O] = Composition[A, B] {type C = O}

    def apply[A, B, O](c: (A, B) => O): Aux[A, B, O] =
      new Composition[A, B] {
        override type C = O
        val gc = c
      }
  }

}
