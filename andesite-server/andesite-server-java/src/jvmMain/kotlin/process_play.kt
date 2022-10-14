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

package andesite.java

import andesite.java.game.handleChat
import andesite.java.game.handleChunks
import andesite.java.game.handleKeepAlive
import andesite.java.game.handleMove
import andesite.java.game.handlePackets
import andesite.java.player.Session
import andesite.java.player.sendPacket
import andesite.java.server.JavaMinecraftServer
import andesite.player.JavaPlayer
import andesite.player.PlayerJoinEvent
import andesite.player.PlayerQuitEvent
import andesite.protocol.java.v756.GameMode
import andesite.protocol.java.v756.JoinGamePacket
import andesite.protocol.java.v756.PlayerPositionAndLookPacket
import andesite.protocol.java.v756.PreviousGameMode
import andesite.protocol.misc.Identifier
import andesite.protocol.types.VarInt
import io.ktor.network.sockets.awaitClosed
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.kotlin.logger

private val logger = logger("andesite.handlers.Play")

internal suspend fun JavaMinecraftServer.processPlay(session: Session, player: JavaPlayer) {
  session.sendPacket(
    JoinGamePacket(
      entityId = 0,
      isHardcore = false,
      gameMode = GameMode.Adventure,
      previousGameMode = PreviousGameMode.Unknown,
      worlds = listOf(Identifier("world")),
      dimensionCodec = dimensionCodec,
      dimension = dimension,
      world = Identifier("world"),
      hashedSeed = 0,
      maxPlayers = VarInt(20),
      viewDistance = VarInt(32),
      reducedDebugInfo = false,
      enableRespawnScreen = false,
      isDebug = false,
      isFlat = true,
    ),
  )

  addPlayer(player)

  session.sendPacket(
    PlayerPositionAndLookPacket(
      x = 0.0,
      y = 50.0,
      z = 0.0,
      yaw = 0f,
      pitch = 0f,
      flags = 0x00,
      teleportId = VarInt(0),
      dismountVehicle = false,
    ),
  )

  publish(PlayerJoinEvent(player))

  coroutineScope {
    launch(CoroutineName("out/sendKeepAlive")) { handleKeepAlive(session) }
    launch(CoroutineName("out/sendChunk")) { handleChunks(session, player) }
    launch(CoroutineName("in/listenChat")) { handleChat(session, player) }
    launch(CoroutineName("in/listenMove")) { handleMove(session, player) }
    launch(CoroutineName("io/handleDisconnect")) {
      session.socket.awaitClosed()

      removePlayer(player)

      publish(PlayerQuitEvent(player))
    }
  }

  withContext(CoroutineName("in/listenPackets")) {
    handlePackets(this, session)
  }
}
