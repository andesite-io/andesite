package com.gabrielleeg1.javarock.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.OutputStreamAppender
import org.fusesource.jansi.AnsiConsole
import java.io.PrintStream
import java.time.format.DateTimeFormatter

class LogAppender : OutputStreamAppender<ILoggingEvent>() {
  init {
    encoder = LogEncoder(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
  }

  override fun start() {
    outputStream = runCatching {
      AnsiConsole.wrapSystemOut(PrintStream(outputStream))
    }.getOrElse { System.out }

    super.start()
  }
}
