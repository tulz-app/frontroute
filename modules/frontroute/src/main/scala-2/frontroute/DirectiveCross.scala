package frontroute

import app.tulz.tuplez.ApplyConverter
import frontroute.ops.DirectiveOfOptionOps

trait DirectiveCross extends ApplyConverters[Route] {

  implicit def addDirectiveApply[L](directive: Directive[L])(implicit hac: ApplyConverter[L, Route]): hac.In => Route = { subRoute => (location, previous, state, baseName) =>
    directive.tapply(hac(subRoute))(location, previous, state, baseName)
  }

  implicit def addNullaryDirectiveApply(directive: Directive0): Route => Route = { subRoute => (location, previous, state, baseName) =>
    directive.tapply(_ => subRoute)(location, previous, state, baseName)
  }

  implicit def addDirectiveExecute[L](directive: Directive[L]): DirectiveExecute[L => Unit] = new DirectiveExecute[L => Unit] {
    def execute(run: L => Unit): Route = {
      directive.tapply { l =>
        runEffect {
          run(l)
        }
      }
    }
  }

  implicit def addNullaryDirectiveExecute(directive: Directive0): DirectiveUnitExecute = new DirectiveUnitExecute {
    def execute(run: => Unit): Route = {
      directive.tapply { _ =>
        runEffect {
          run
        }
      }
    }
  }

  implicit def directiveOfOptionSyntax[A](underlying: Directive[Option[A]]): DirectiveOfOptionOps[A] = new DirectiveOfOptionOps[A](underlying)

}
