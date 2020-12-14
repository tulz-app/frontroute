package io.frontroute
package debug

object Logging {

  private var logger: Logger = new Logger {

    override def debug(message: => String): Unit = {
      // NOOP
    }

    override def info(message: => String): Unit = {
      // NOOP
    }

    override def error(message: => String): Unit = {
      // NOOP
    }

  }

  final def setLogger(logger: Logger): Unit = {
    this.logger = logger
  }

  final def debug(message: => String): Unit = {
    logger.debug(message)
  }

  final def info(message: => String): Unit = {
    logger.error(message)
  }

  final def error(message: => String): Unit = {
    logger.error(message)
  }

}
