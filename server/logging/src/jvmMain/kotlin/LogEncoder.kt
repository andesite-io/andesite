package com.gabrielleeg1.javarock.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.encoder.EncoderBase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class LogEncoder(private val dateFormatter: DateTimeFormatter) : EncoderBase<ILoggingEvent>() {
  override fun headerBytes() = byteArrayOf()
  override fun footerBytes() = byteArrayOf()

  override fun encode(event: ILoggingEvent): ByteArray {
    val time = dateFormatter.format(LocalDateTime.now())
    val level = LogLevel.fromLogbackLevel(event.level)

    val name = event.loggerName.split(".").let { it[it.size - 1] }
    val thread = event.threadName

    val message = if (event.throwableProxy == null) {
      event.message + Reset
    } else {
      event.throwableProxy!!.stackTraceElementProxyArray?.let {
        val cause = event.throwableProxy

        val causeMessage = "${cause?.className}: ${cause?.message}"

        listOf(LINE_SEPARATOR + Red + causeMessage, *it)
          .joinToString(separator = "") { element ->
            "\t$Red$element$LINE_SEPARATOR"
          }
      }
    }

    return FORMAT.format(time, level, name, thread, message).toByteArray()
  }

  companion object {
    private const val LINE_SEPARATOR = "\r\n"
    private const val FORMAT =
      "$Gray[$LightRed%s $Reset%s $LightGray%s $Reset%s$Gray]$Reset %s$Reset$LINE_SEPARATOR"
  }
}
