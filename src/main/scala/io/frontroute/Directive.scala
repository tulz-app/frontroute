package io.frontroute

import com.raquo.airstream.eventstream.EventStream
import com.raquo.airstream.signal.Signal
import com.raquo.airstream.signal.Var

class Directive[L](
  val tapply: (L => Route) => Route
) {
  self =>

  def flatMap[R](next: L => Directive[R]): Directive[R] = {
    Directive[R] { inner =>
      self.tapply { value => (location, previous, state) =>
        next(value).tapply(inner)(location, previous, state.path(".flatMap"))
      }
    }
  }

  def map[R](f: L => R): Directive[R] =
    Directive[R] { inner =>
      self.tapply { value => (location, previous, state) =>
        inner(f(value))(location, previous, state.path(".map"))
      }
    }

  def &[R](magnet: ConjunctionMagnet[L]): magnet.Out = magnet(this)

  def |[U >: L](other: Directive[L]): Directive[L] = {
    Directive[L] { inner => (ctx, previous, state) =>
      self
        .tapply { value => (ctx, previous, state) =>
          inner(value)(ctx, previous, state.leaveDisjunction())
        }(ctx, previous, state.enterDisjunction())
        .flatMap {
          case complete: RouteResult.Complete => EventStream.fromValue(complete, emitOnce = false)
          case RouteResult.Rejected =>
            other.tapply { value => (ctx, previous, state) =>
              inner(value)(ctx, previous, state.leaveDisjunction())
            }(ctx, previous, state.enterDisjunction())
        }
    }

  }

  def collect[R](f: PartialFunction[L, R]): Directive[R] =
    Directive[R] { inner =>
      self.tapply { value => (location, previous, state) =>
        if (f.isDefinedAt(value)) {
          inner(f(value))(location, previous, state.path(".collect"))
        } else {
          rejected
        }
      }
    }

  def filter(predicate: L => Boolean): Directive[L] =
    Directive[L] { inner =>
      self.tapply { value => (location, previous, state) =>
        if (predicate(value)) {
          inner(value)(location, previous, state.path(".filter"))
        } else {
          rejected
        }
      }
    }

  def signal: Directive[Signal[L]] =
    new Directive[Signal[L]]({ inner => (ctx, previous, state) =>
      this.tapply {
        value => // TODO figure this out, when this is run, enter is not yet called
          (ctx, previous, state) =>
            val next = state.unsetValue().path(".signal")
            previous.getValue[Var[L]](next.path) match {
              case None =>
                val var$ = Var(value)
                inner(var$.signal)(ctx, previous, next.setValue(var$))
              case Some(var$) =>
                var$.writer.onNext(value)
                inner(var$.signal)(ctx, previous, next.setValue(var$))
            }
      }(ctx, previous, state)
    })

}

object Directive {

  def apply[L](f: (L => Route) => Route): Directive[L] = {
    new Directive[L](inner =>
      (ctx, previous, state) =>
        f(value =>
          (ctx, previous, state) => {
            inner(value)(ctx, previous, state)
          }
        )(ctx, previous, state)
    )
  }

}
