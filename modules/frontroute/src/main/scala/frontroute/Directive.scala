package frontroute

import com.raquo.laminar.api.L.*

class Directive[+L](
  val tapply: (L => Route) => Route
) {
  self =>

  def flatMap[R](next: L => Directive[R]): Directive[R] = {
    Directive[R] { inner =>
      self.tapply { value => (location, previous, state, baseName) =>
        next(value).tapply(inner)(location, previous, state.enter, baseName)
      }
    }
  }

  def map[R](f: L => R): Directive[R] =
    Directive[R] { inner =>
      self.tapply { value => (location, previous, state, baseName) =>
        val mapped = f(value)
        inner(mapped)(location, previous, state.enterAndSet(mapped), baseName)
      }
    }

  def tap(body: L => Unit): Directive[L] =
    Directive[L] { inner =>
      self.tapply { value => (location, previous, state, baseName) =>
        body(value)
        inner(value)(location, previous, state.enterAndSet(value), baseName)
      }
    }

  def emap[R](f: L => Either[Any, R]): Directive[R] =
    this.flatMap { value =>
      f(value).fold(
        _ => reject,
        r => provide(r)
      )
    }

  def opt: Directive[Option[L]] =
    this.map(v => Option(v)) | provide(None)

  @inline def some: Directive[Option[L]] = map(Some(_))

  @inline def none[R]: Directive[Option[R]] = mapTo(Option.empty[R])

  @inline def mapTo[R](otherValue: => R): Directive[R] = map(_ => otherValue)

  def &[LL >: L](magnet: ConjunctionMagnet[LL]): magnet.Out = magnet(this)

  def |[LL >: L](other: Directive[LL]): Directive[LL] =
    Directive[LL] { inner => (location, previous, state, baseName) =>
      self
        .tapply { value => (location, previous, state, baseName) =>
          inner(value)(location, previous, state.leaveDisjunction, baseName)
        }(location, previous, state.enterDisjunction, baseName) match {
        case RouteResult.Matched(state, location, consumed, result) => RouteResult.Matched(state, location, consumed, result)
        case RouteResult.RunEffect(state, location, consumed, run)  => RouteResult.RunEffect(state, location, consumed, run)
        case RouteResult.Rejected                                   =>
          other.tapply { value => (location, previous, state, baseName) =>
            inner(value)(location, previous, state.leaveDisjunction, baseName)
          }(location, previous, state.enterDisjunction, baseName)
      }
    }

  def collect[R](f: PartialFunction[L, R]): Directive[R] =
    Directive[R] { inner =>
      self.tapply { value => (location, previous, state, baseName) =>
        if (f.isDefinedAt(value)) {
          val mapped = f(value)
          inner(mapped)(location, previous, state.enterAndSet(mapped), baseName)
        } else {
          rejected
        }
      }
    }

  def filter(predicate: L => Boolean): Directive[L] =
    Directive[L] { inner =>
      self.tapply { value => (location, previous, state, baseName) =>
        if (predicate(value)) {
          inner(value)(location, previous, state.enter, baseName)
        } else {
          rejected
        }
      }
    }

  def signal: Directive[StrictSignal[L]] =
    new Directive[StrictSignal[L]]({ inner => (location, previous, state, baseName) =>
      this.tapply { value => (location, previous, state, baseName) =>
        val next = state.unsetValue().enter
        previous.getValue[Var[L]](next.path.key) match {
          case None              =>
            val newVar = Var(value)
            inner(newVar.signal)(location, previous, next.setValue(newVar), baseName)
          case Some(existingVar) =>
            existingVar.set(value)
            inner(existingVar.signal)(location, previous, next.setValue(existingVar), baseName)
        }
      }(location, previous, state, baseName)
    })

}

object Directive extends DirectiveCross {

  def apply[L](f: (L => Route) => Route): Directive[L] = {
    new Directive[L](inner =>
      (location, previous, state, baseName) =>
        f(value =>
          (location, previous, state, baseName) => {
            inner(value)(location, previous, state, baseName)
          }
        )(location, previous, state, baseName)
    )
  }

}
