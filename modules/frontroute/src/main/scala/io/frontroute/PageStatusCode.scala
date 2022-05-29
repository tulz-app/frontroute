package io.frontroute

sealed trait PageStatusCode extends Product with Serializable

object PageStatusCode {

  case object Ok       extends PageStatusCode
  case object Error    extends PageStatusCode
  case object NotFound extends PageStatusCode

}
