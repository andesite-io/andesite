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

package andesite.java.game

import andesite.event.on
import andesite.java.convertChunk
import andesite.java.player.Session
import andesite.java.player.sendPacket
import andesite.java.server.JavaMinecraftServer
import andesite.player.JavaPlayer
import andesite.player.PlayerMoveEvent
import andesite.protocol.java.v756.PlayerPositionAndLookPacket
import andesite.protocol.types.VarInt
import org.apache.logging.log4j.kotlin.logger

private val logger = logger("andesite.java.game.Chunk")

internal suspend fun JavaMinecraftServer.handleChunks(session: Session, player: JavaPlayer) {
  // Load new chunks
  player.on<PlayerMoveEvent> {
    val chunk = player.location.world.getChunkAt(
      player.location.x.toInt(),
      player.location.z.toInt(),
    )
    if (chunk != null) {
      session.sendPacket(convertChunk(chunk))
    }

    if (newLocation.y < 0) {
      session.sendPacket(
        PlayerPositionAndLookPacket(
          x = 0.0,
          y = 50.0, // TODO: Implement better solution from falling in the void
          z = 0.0,
          yaw = 0f,
          pitch = 0f,
          flags = 0x1D, // Only Y position isn't relative
          teleportId = VarInt(0),
          dismountVehicle = false,
        ),
      )
    }
  }
}
