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

@file:OptIn(ExperimentalTime::class, ExperimentalTime::class)

package andesite.server.java

import com.benasher44.uuid.uuid4
import andesite.player.JavaPlayer
import andesite.protocol.currentTimeMillis
import andesite.protocol.java.handshake.HandshakePacket
import andesite.protocol.java.handshake.PingPacket
import andesite.protocol.java.handshake.Players
import andesite.protocol.java.handshake.PongPacket
import andesite.protocol.java.handshake.Response
import andesite.protocol.java.handshake.ResponsePacket
import andesite.protocol.java.handshake.Version
import andesite.protocol.java.login.LoginStartPacket
import andesite.protocol.java.login.LoginSuccessPacket
import andesite.protocol.java.v756.GameMode
import andesite.protocol.java.v756.JoinGamePacket
import andesite.protocol.java.v756.KeepAlivePacket
import andesite.protocol.java.v756.PlayerPositionAndLookPacket
import andesite.protocol.java.v756.PreviousGameMode
import andesite.protocol.java.v756.ServerKeepAlivePacket
import andesite.protocol.misc.Chat
import andesite.protocol.misc.Identifier
import andesite.protocol.types.VarInt
import andesite.world.Location
import andesite.server.java.player.JavaPlayerImpl
import andesite.server.java.player.Session
import andesite.server.java.player.awaitPacket
import andesite.server.java.player.receivePacket
import andesite.server.java.player.sendPacket
import io.ktor.network.sockets.isClosed
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.logging.log4j.kotlin.logger
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private val logger = logger("andesite.Handlers")

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

  val spawn = Location(0.0, 10.0, 0.0, 0.0f, 0.0f)

  launch(Job()) {
    while (!session.socket.isClosed) {
      val packet = session.acceptPacket() ?: continue

      session.inboundPacketChannel.send(packet)
    }
  }

  launch(Job()) {
    while (!session.socket.isClosed) {
      delay(20.seconds)

      try {
        session.sendPacket(KeepAlivePacket(currentTimeMillis()))
        session.awaitPacket<ServerKeepAlivePacket>()
      } catch (error: Throwable) {
        logger.error(error) { "Player [$player] keep alive thread thrown an error" }
      }
    }
  }

  launch(Job()) {
    for (x in -1 until ((spawn.x * 2) / 16 + 1).toInt()) {
      for (z in -1 until ((spawn.z * 2) / 16 + 1).toInt()) {
        val chunk = world.getChunkAt(x, z) ?: continue

        session.sendPacket(chunk.toPacket())
      }
    }
  }
}
