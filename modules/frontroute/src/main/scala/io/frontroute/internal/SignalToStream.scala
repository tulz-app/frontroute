package io.frontroute.internal

import com.raquo.laminar.api.L._

private[frontroute] object SignalToStream {

  def apply[T](signal: Signal[T]): EventStream[T] =
    EventStream.merge(EventStream.fromValue(()).sample(signal), signal.changes)

}
