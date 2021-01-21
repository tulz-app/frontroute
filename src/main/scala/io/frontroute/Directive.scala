package io.frontroute

import com.raquo.airstream.eventstream.EventStream
import com.raquo.airstream.signal.Signal
import com.raquo.airstream.signal.Var
import io.frontroute.ops.DirectiveOfOptionOps

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
        val mapped = f(value)
        inner(mapped)(location, previous, state.path(".map").setValue(mapped))
      }
    }

  @inline def some: Directive[Option[L]] = map(Some(_))

  @inline def none[R]: Directive[Option[R]] = mapTo(Option.empty[R])

  def mapTo[R](otherValue: => R): Directive[R] =
    Directive[R] { inner =>
      self.tapply { _ => (location, previous, state) =>
        inner(otherValue)(location, previous, state.path(".mapTo"))
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
                val newVar = Var(value)
                inner(newVar.signal)(ctx, previous, next.setValue(newVar))
              case Some(existingVar) =>
                existingVar.writer.onNext(value)
                inner(existingVar.signal)(ctx, previous, next.setValue(existingVar))
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

  implicit def directiveOfOptionSyntax[A](underlying: Directive[Option[A]]): DirectiveOfOptionOps[A] = new DirectiveOfOptionOps[A](underlying)

}
