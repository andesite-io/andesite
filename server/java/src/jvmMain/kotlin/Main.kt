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

@file:OptIn(ExperimentalSerializationApi::class)

package com.gabrielleeg1.javarock.server.java

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.gabrielleeg1.javarock.api.player.JavaPlayer
import com.gabrielleeg1.javarock.api.protocol.chat.Chat
import com.gabrielleeg1.javarock.api.protocol.java.JavaPacket
import com.gabrielleeg1.javarock.api.protocol.java.handshake.HandshakePacket
import com.gabrielleeg1.javarock.api.protocol.java.handshake.NextState
import com.gabrielleeg1.javarock.api.protocol.java.handshake.PingPacket
import com.gabrielleeg1.javarock.api.protocol.java.handshake.Players
import com.gabrielleeg1.javarock.api.protocol.java.handshake.PongPacket
import com.gabrielleeg1.javarock.api.protocol.java.handshake.Response
import com.gabrielleeg1.javarock.api.protocol.java.handshake.ResponsePacket
import com.gabrielleeg1.javarock.api.protocol.java.handshake.Version
import com.gabrielleeg1.javarock.api.protocol.java.login.LoginStartPacket
import com.gabrielleeg1.javarock.api.protocol.java.login.LoginSuccessPacket
import com.gabrielleeg1.javarock.api.protocol.java.play.ChunkDataPacket
import com.gabrielleeg1.javarock.api.protocol.java.play.GameMode
import com.gabrielleeg1.javarock.api.protocol.java.play.JoinGamePacket
import com.gabrielleeg1.javarock.api.protocol.java.play.PlayerPositionAndLookPacket
import com.gabrielleeg1.javarock.api.protocol.java.play.PreviousGameMode
import com.gabrielleeg1.javarock.api.protocol.resource
import com.gabrielleeg1.javarock.api.protocol.serialization.MinecraftCodec
import com.gabrielleeg1.javarock.api.protocol.serializers.UuidSerializer
import com.gabrielleeg1.javarock.api.protocol.types.VarInt
import com.gabrielleeg1.javarock.api.protocol.writeVarInt
import com.gabrielleeg1.javarock.api.world.Location
import com.gabrielleeg1.javarock.api.world.anvil.AnvilChunk
import com.gabrielleeg1.javarock.api.world.anvil.readAnvilWorld
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.writeFully
import io.ktor.utils.io.core.writeShort
import io.ktor.utils.io.core.writeUByte
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import mu.KotlinLogging
import net.benwoodworth.knbt.Nbt
import net.benwoodworth.knbt.NbtCompression
import net.benwoodworth.knbt.NbtVariant
import java.io.File
import java.util.concurrent.Executors

private val context = Executors.newCachedThreadPool().asCoroutineDispatcher()

private val logger = KotlinLogging.logger { }

private val nbt = Nbt {
  variant = NbtVariant.Java
  compression = NbtCompression.None
  ignoreUnknownKeys = true
}

private val world = readAnvilWorld(File(resource("world")))

suspend fun main(): Unit = withContext(context) {
  val selector = ActorSelectorManager(Dispatchers.IO)
  val server = aSocket(selector).tcp().bind(hostname = "0.0.0.0", port = 25565)
  val codec = MinecraftCodec {
    protocolVersion = 756
    json = Json {
      prettyPrint = true
    }
    serializersModule = SerializersModule {
      contextual(UuidSerializer)
    }
  }

  logger.info { "Server started at 0.0.0.0:25565" }

  while (true) {
    val session = Session(codec, server.accept())

    launch {
      try {
        val handshake = session.receivePacket<HandshakePacket>()

        when (handshake.nextState) {
          NextState.Status -> handleStatus(session, handshake)
          NextState.Login -> handlePlay(session, handleLogin(session, handshake))
        }
      } catch (error: Throwable) {
        logger.error(error) {
          "Error thrown while handling connection ${session.socket.remoteAddress}"
        }

        withContext(Dispatchers.IO) {
          session.socket.close()
        }
      }
    }
  }
}

class JavaPlayerImpl(
  override val id: Uuid,
  override val protocol: Int,
  override val username: String,
  val session: Session,
) : JavaPlayer {
  override suspend fun sendPacket(packet: JavaPacket, queue: Boolean) {
    session.sendPacket(packet)
  }
}

private suspend fun handlePlay(session: Session, player: JavaPlayer): Unit = coroutineScope {
  session.sendPacket(
    JoinGamePacket(
      entityId = 0,
      isHardcore = false,
      gameMode = GameMode.Adventure,
      previousGameMode = PreviousGameMode.Unknown,
      worlds = listOf("world"),
      dimensionCodec = nbt.decodeFromByteArray(File(resource("dimension_codec.nbt")).readBytes()),
      dimension = nbt.decodeFromByteArray(File(resource("dimension.nbt")).readBytes()),
      world = "world",
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

private suspend fun handleLogin(session: Session, handshake: HandshakePacket): JavaPlayer {
  val id = uuid4()
  val protocol = handshake.protocolVersion.toInt()
  val (username) = session.receivePacket<LoginStartPacket>()

  session.sendPacket(LoginSuccessPacket(id, username))

  return JavaPlayerImpl(id, protocol, username, session)
}

private suspend fun handleStatus(session: Session, handshake: HandshakePacket) {
  session.sendPacket(
    ResponsePacket(
      Response(
        version = Version(name = "Javarock for 1.17", protocol = handshake.protocolVersion.toInt()),
        players = Players(max = 20, online = 0),
        description = Chat.of("&eHello, world"),
      ),
    ),
  )

  session.receivePacket<PingPacket>()

  session.sendPacket(PongPacket())
}

private fun AnvilChunk.toPacket(): ChunkDataPacket {
  val data = buildPacket {
    for (section in sections) {
      val blocks = section.blockStates.toList()
        .map(Long::toInt)
        .mapNotNull(section.palette::getOrNull)

      writeShort(blocks.size.toShort())
      writeUByte(4.toUByte())

      writeVarInt(section.palette.size)
      for (item in blocks) {
        writeFully(nbt.encodeToByteArray(item))
      }

      writeVarInt(section.blockStates.size)
      writeFully(section.blockStates)
    }
  }.readBytes()

  return ChunkDataPacket(
    x, z,
    LongArray(sections.size) { 1 },
    heightmaps,
    biomes,
    data,
    tileEntities,
  )
}
