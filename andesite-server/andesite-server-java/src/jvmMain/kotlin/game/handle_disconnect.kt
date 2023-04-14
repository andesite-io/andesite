/*
 *    Copyright 2023 Gabrielle Guimarães de Oliveira
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

package andesite.java.game

import andesite.java.player.Session
import andesite.java.server.JavaMinecraftServer
import andesite.player.JavaPlayer
import andesite.player.PlayerQuitEvent
import io.ktor.network.sockets.awaitClosed
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import org.apache.logging.log4j.kotlin.logger

private val logger = logger("andesite.java.game.Disconnect")

internal suspend fun JavaMinecraftServer.handleDisconnect(
  scope: CoroutineScope,
  session: Session,
  player: JavaPlayer,
) {
  session.socket.awaitClosed()

  removePlayer(player)
  publish(PlayerQuitEvent(player))

  scope.cancel(CancellationException("Player disconnected"))
}
