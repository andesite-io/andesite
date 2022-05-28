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

package andesite.server.java.handlers

import andesite.player.JavaPlayer
import andesite.player.PlayerJoinEvent
import andesite.protocol.java.v756.GameMode
import andesite.protocol.java.v756.JoinGamePacket
import andesite.protocol.java.v756.PlayerPositionAndLookPacket
import andesite.protocol.java.v756.PreviousGameMode
import andesite.protocol.misc.Identifier
import andesite.protocol.types.VarInt
import andesite.server.java.player.Session
import andesite.server.java.player.sendPacket
import andesite.server.java.server.JavaMinecraftServer
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.apache.logging.log4j.kotlin.logger

private val logger = logger("andesite.handlers.Play")

internal fun JavaMinecraftServer.handlePlay(session: Session, player: JavaPlayer): Job =
  session.launch(CoroutineName("handlePlay")) {
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

    launch(CoroutineName("handlePackets")) { handlePackets(session) }
    launch(CoroutineName("handleKeepAlive")) { handleKeepAlive(session) }
    launch(CoroutineName("handleChunkMovement")) { handleChunkMovement(session) }
    launch(CoroutineName("handleChat")) { handleChat(session, player) }
  }
