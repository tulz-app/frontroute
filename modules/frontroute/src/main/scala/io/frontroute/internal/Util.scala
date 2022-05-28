package io.frontroute
package internal

import com.raquo.airstream.core.EventStream

private[frontroute] object Util {

  private[frontroute] def rejected: EventStream[RouteResult[Nothing]] = EventStream.fromValue(RouteResult.Rejected)

}
