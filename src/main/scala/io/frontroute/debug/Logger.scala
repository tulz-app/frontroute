package io.frontroute
package debug

import org.scalajs.dom

trait Logger {

  def debug(message: => String): Unit
  def info(message: => String): Unit
  def error(message: => String): Unit

}

object Logger {

  val consoleLogger: Logger = new Logger {

    final def debug(message: => String): Unit = dom.console.debug(message)

    final def info(message: => String): Unit = dom.console.info(message)

    final def error(message: => String): Unit = dom.console.error(message)

  }

}
