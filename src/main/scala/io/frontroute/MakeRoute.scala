package io.frontroute

import com.raquo.airstream.core.Signal
import com.raquo.airstream.state.Var

trait MakeRoute {

  def makeRoute[A](make: (A => Route) => Route): (Signal[Option[A]], Route) = {
    val routeVar = Var[Option[A]](Option.empty)
    val route = make(a =>
      complete {
        routeVar.writer.onNext(Some(a))
      }
    )
    (routeVar.signal, route)
  }

  def makeRouteWithCallback[A](onRoute: () => Unit)(make: (A => Route) => Route): (Signal[Option[A]], Route) = {
    makeRoute[A] { (render: A => Route) =>
      make(a => {
        onRoute()
        render(a)
      })
    }
  }

}
