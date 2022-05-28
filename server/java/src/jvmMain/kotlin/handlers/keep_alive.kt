/*
 *    Copyright 2022 Gabrielle Guimarães de Oliveira
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

@file:OptIn(ExperimentalTime::class)

package andesite.server.java.handlers

import andesite.protocol.currentTimeMillis
import andesite.protocol.java.v756.KeepAlivePacket
import andesite.server.java.player.Session
import andesite.server.java.player.sendPacket
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.delay
import org.apache.logging.log4j.kotlin.logger

private val logger = logger("andesite.handlers.KeepAlive")

internal suspend fun handleKeepAlive(session: Session) {
  while (true) {
    delay(20.seconds)

    try {
      session.sendPacket(KeepAlivePacket(currentTimeMillis()))
    } catch (_: Throwable) {
      break
    }
  }
}
