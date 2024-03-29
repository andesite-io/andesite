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

package andesite.java.game

import andesite.java.player.Session
import andesite.java.player.sendPacket
import andesite.protocol.currentTimeMillis
import andesite.protocol.java.v756.KeepAlivePacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.kotlin.logger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

private val logger = logger("andesite.java.game.KeepAlive")

internal suspend fun handleKeepAlive(session: Session) {
  while (true) {
    delay(500.milliseconds)

    try {
      session.sendPacket(KeepAlivePacket(currentTimeMillis()))
    } catch (error: Throwable) {
      withContext(Dispatchers.IO) {
        session.socket.close()
      }

      break
    }
  }
}
