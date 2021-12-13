package com.gabrielleeg1.andesite.logging

import ch.qos.logback.classic.Level

internal enum class LogLevel(private val level: String, private val color: String) {
  Info("INFO", LightGreen),
  Warn("WARN", Yellow),
  Error("ERROR", Red),
  Debug("DEBUG", Blue),
  Trace("TRACE", LightBlue),
  All("ALL", Trace.color),
  None("", "");

  override fun toString(): String = "$color%-5s$Reset".format(level)

  companion object {
    fun fromLogbackLevel(level: Level): LogLevel = values()
      .find { it.level.equals(level.levelStr, ignoreCase = true) } ?: None
  }
}
