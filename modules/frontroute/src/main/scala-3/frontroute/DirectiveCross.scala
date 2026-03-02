package frontroute

trait DirectiveCross {

  extension (d: Directive[Unit]) {

    def apply(l: => Route): Route = d.tapply(_ => l)

    def execute(run: => Unit): Route =
      d.tapply { _ =>
        runEffect {
          run
        }
      }

  }

  extension [L](d: Directive[L]) {

    def apply(l: L => Route): Route = d.tapply(l)

    def execute(run: L => Unit): Route =
      d.tapply { l =>
        runEffect {
          run(l)
        }
      }

  }

  extension [A](underlying: Directive[Option[A]]) {

    def mapOption[R](f: A => R): Directive[Option[R]] = underlying.map(_.map(f))

    def default(v: => A): Directive[A] = underlying.map(_.getOrElse(v))

    def collectOption[R](f: PartialFunction[A, R]): Directive[Option[R]] = underlying.map(_.collect(f))

  }

}
