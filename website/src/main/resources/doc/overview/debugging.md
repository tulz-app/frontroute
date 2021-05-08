# Debugging/logging

`frontroute` can provide a some help when debugging you routes by logging the rejected logs.
By default, the logger will not print anything (noop). To enable the `dom.console` output, set the
logger: `Logging.setLogger(Logger.consoleLogger)` (or implement your own).

The `debug` directive prints the messages using the same logger as well.
