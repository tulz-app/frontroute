package io.frontroute

import com.raquo.laminar.api.L._

class Directive[L](
  val tapply: (L => Route) => Route
) {
  self =>

  def flatMap[R](next: L => Directive[R]): Directive[R] = {
    Directive[R] { inner =>
      self.tapply { value => (location, previous, state) =>
        next(value).tapply(inner)(location, previous, state.enter)
      }
    }
  }

  def map[R](f: L => R): Directive[R] =
    Directive[R] { inner =>
      self.tapply { value => (location, previous, state) =>
        val mapped = f(value)
        inner(mapped)(location, previous, state.enterAndSet(mapped))
      }
    }

  @inline def some: Directive[Option[L]] = map(Some(_))

  @inline def none[R]: Directive[Option[R]] = mapTo(Option.empty[R])

  @inline def mapTo[R](otherValue: => R): Directive[R] = map(_ => otherValue)

  def &(magnet: ConjunctionMagnet[L]): magnet.Out = magnet(this)

  def |(other: Directive[L]): Directive[L] = {
    Directive[L] { inner => (location, previous, state) =>
      self
        .tapply { value => (location, previous, state) =>
          inner(value)(location, previous, state.leaveDisjunction)
        }(location, previous, state.enterDisjunction)
        .flatMap {
          case RouteResult.Matched(state, location, result) => Val(RouteResult.Matched(state, location, result))
          case RouteResult.Rejected                         =>
            other.tapply { value => (location, previous, state) =>
              inner(value)(location, previous, state.leaveDisjunction)
            }(location, previous, state.enterDisjunction)
        }
    }

  }

  def collect[R](f: PartialFunction[L, R]): Directive[R] =
    Directive[R] { inner =>
      self.tapply { value => (location, previous, state) =>
        if (f.isDefinedAt(value)) {
          val mapped = f(value)
          inner(mapped)(location, previous, state.enterAndSet(mapped))
        } else {
          rejected
        }
      }
    }

  def filter(predicate: L => Boolean): Directive[L] =
    Directive[L] { inner =>
      self.tapply { value => (location, previous, state) =>
        if (predicate(value)) {
          inner(value)(location, previous, state.enter)
        } else {
          rejected
        }
      }
    }

  def signal: Directive[StrictSignal[L]] =
    new Directive[StrictSignal[L]]({ inner => (location, previous, state) =>
      this.tapply {
        value => // TODO figure this out, when this is run, enter is not yet called
          (location, previous, state) =>
            val next = state.unsetValue().enter
            previous.getValue[Var[L]](next.path.key) match {
              case None              =>
                val newVar = Var(value)
                inner(newVar.signal)(location, previous, next.setValue(newVar))
              case Some(existingVar) =>
                existingVar.set(value)
                inner(existingVar.signal)(location, previous, next.setValue(existingVar))
            }
      }(location, previous, state)
    })

}

object Directive {

  def apply[L](f: (L => Route) => Route): Directive[L] = {
    new Directive[L](inner =>
      (location, previous, state) =>
        f(value =>
          (location, previous, state) => {
            inner(value)(location, previous, state)
          }
        )(location, previous, state)
    )
  }

}
