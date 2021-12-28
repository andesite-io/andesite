/*
 *    Copyright 2021 Gabrielle Guimarães de Oliveira
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

package com.gabrielleeg1.andesite.server.java

import com.benasher44.uuid.uuid4
import com.gabrielleeg1.andesite.api.player.JavaPlayer
import com.gabrielleeg1.andesite.api.protocol.java.handshake.HandshakePacket
import com.gabrielleeg1.andesite.api.protocol.java.handshake.PingPacket
import com.gabrielleeg1.andesite.api.protocol.java.handshake.Players
import com.gabrielleeg1.andesite.api.protocol.java.handshake.PongPacket
import com.gabrielleeg1.andesite.api.protocol.java.handshake.Response
import com.gabrielleeg1.andesite.api.protocol.java.handshake.ResponsePacket
import com.gabrielleeg1.andesite.api.protocol.java.handshake.Version
import com.gabrielleeg1.andesite.api.protocol.java.login.LoginStartPacket
import com.gabrielleeg1.andesite.api.protocol.java.login.LoginSuccessPacket
import com.gabrielleeg1.andesite.api.protocol.java.play.GameMode
import com.gabrielleeg1.andesite.api.protocol.java.play.JoinGamePacket
import com.gabrielleeg1.andesite.api.protocol.java.play.PlayerPositionAndLookPacket
import com.gabrielleeg1.andesite.api.protocol.java.play.PreviousGameMode
import com.gabrielleeg1.andesite.api.protocol.misc.Chat
import com.gabrielleeg1.andesite.api.protocol.misc.Identifier
import com.gabrielleeg1.andesite.api.protocol.types.VarInt
import com.gabrielleeg1.andesite.api.world.Location
import com.gabrielleeg1.andesite.server.java.player.JavaPlayerImpl
import com.gabrielleeg1.andesite.server.java.player.Session
import com.gabrielleeg1.andesite.server.java.player.receivePacket
import com.gabrielleeg1.andesite.server.java.player.sendPacket
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromByteArray
import java.io.File

internal suspend fun handleLogin(session: Session, handshake: HandshakePacket): JavaPlayer {
  val id = uuid4()
  val protocol = handshake.protocolVersion.toInt()
  val (username) = session.receivePacket<LoginStartPacket>()

  session.sendPacket(LoginSuccessPacket(id, username))

  return JavaPlayerImpl(id, protocol, username, session)
}

internal suspend fun handleStatus(session: Session, handshake: HandshakePacket) {
  session.sendPacket(
    ResponsePacket(
      Response(
        version = Version(name = "Andesite for 1.17", protocol = handshake.protocolVersion.toInt()),
        players = Players(max = 20, online = 0),
        description = Chat.of("&eHello, world"),
      ),
    ),
  )

  session.receivePacket<PingPacket>()

  session.sendPacket(PongPacket())
}

internal suspend fun handlePlay(session: Session, player: JavaPlayer): Unit = coroutineScope {
  session.sendPacket(
    JoinGamePacket(
      entityId = 0,
      isHardcore = false,
      gameMode = GameMode.Adventure,
      previousGameMode = PreviousGameMode.Unknown,
      worlds = listOf(Identifier("world")),
      dimensionCodec = nbt.decodeFromByteArray(File(resource("dimension_codec.nbt")).readBytes()),
      dimension = nbt.decodeFromByteArray(File(resource("dimension.nbt")).readBytes()),
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

  val spawn = Location.Empty

  launch(Job()) {
    for (x in -1 until ((spawn.x * 2) / 16 + 1).toInt()) {
      for (z in -1 until ((spawn.z * 2) / 16 + 1).toInt()) {
        val chunk = world.getChunkAt(x, z) ?: continue

        session.sendPacket(chunk.toPacket())
      }
    }
  }
}
