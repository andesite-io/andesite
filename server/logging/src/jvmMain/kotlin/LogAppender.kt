package com.gabrielleeg1.andesite.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.OutputStreamAppender
import org.fusesource.jansi.AnsiConsole
import java.io.PrintStream

class LogAppender : OutputStreamAppender<ILoggingEvent>() {
  init {
    encoder = LogEncoder()
  }

  override fun start() {
    outputStream = runCatching {
      AnsiConsole.wrapSystemOut(PrintStream(outputStream))
    }.getOrElse { System.out }

    super.start()
  }
}
