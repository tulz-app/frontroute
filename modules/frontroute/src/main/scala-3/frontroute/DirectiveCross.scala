package frontroute

trait DirectiveCross {

  extension (d: Directive[Unit]) {

    inline def apply(l: => Route): Route = d.tapply(_ => l)

    inline def execute(run: => Unit): Route =
      d.tapply { _ =>
        runEffect {
          run
        }
      }

  }

  extension [L](d: Directive[L]) {

    inline def apply(l: L => Route): Route = d.tapply(l)

    inline def execute(run: L => Unit): Route =
      d.tapply { l =>
        runEffect {
          run(l)
        }
      }

  }

  extension [A](underlying: Directive[Option[A]]) {

    inline def mapOption[R](f: A => R): Directive[Option[R]] = underlying.map(_.map(f))

    inline def default(v: => A): Directive[A] = underlying.map(_.getOrElse(v))

    inline def collectOption[R](f: PartialFunction[A, R]): Directive[Option[R]] = underlying.map(_.collect(f))

  }

}
