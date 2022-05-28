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

package andesite.server.java

import andesite.AndesiteError
import andesite.protocol.java.data.Dimension
import andesite.protocol.java.data.DimensionCodec
import andesite.protocol.java.handshake.HandshakePacket
import andesite.protocol.java.handshake.NextState
import andesite.protocol.java.v756.v756
import andesite.protocol.resource
import andesite.protocol.serialization.MinecraftCodec
import andesite.protocol.serialization.extractMinecraftVersion
import andesite.protocol.serializers.UuidSerializer
import andesite.server.java.game.GameServer
import andesite.server.java.game.handlePlay
import andesite.server.java.player.Session
import andesite.server.java.player.receivePacket
import andesite.world.anvil.AnvilWorld
import andesite.world.anvil.block.BlockRegistry
import andesite.world.anvil.block.readBlockRegistry
import andesite.world.anvil.readAnvilWorld
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.isClosed
import io.ktor.utils.io.ClosedWriteChannelException
import java.net.InetSocketAddress
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import net.benwoodworth.knbt.Nbt
import net.benwoodworth.knbt.NbtCompression
import net.benwoodworth.knbt.NbtVariant
import org.apache.logging.log4j.kotlin.logger

private val logger = logger("andesite.Main")

internal val nbt: Nbt = Nbt {
  variant = NbtVariant.Java
  compression = NbtCompression.None
  ignoreUnknownKeys = true
}

internal val blockRegistry: BlockRegistry =
  readBlockRegistry(resource("v756").resolve("blocks.json").readText())

internal lateinit var world: AnvilWorld

internal val dimensionCodec = nbt.decodeRootTag<DimensionCodec>(resource("dimension_codec.nbt"))

internal val dimension = nbt.decodeRootTag<Dimension>(resource("dimension.nbt"))

suspend fun startAndesite(): Unit = coroutineScope {
  logger.info("Starting andesite...")

  val selector = ActorSelectorManager(Dispatchers.IO)
  val address = InetSocketAddress("127.0.0.1", 25565)
  val server = aSocket(selector).tcp().bind(address)
  val codec = MinecraftCodec.v756 {
    json = Json {
      prettyPrint = true
    }
    serializersModule = SerializersModule {
      contextual(UuidSerializer)
    }
  }

  val protocolVersion = codec.configuration.protocolVersion
  val minecraftVersion = extractMinecraftVersion(codec.configuration.protocolVersion)

  logger.info(
    "Set up minecraft codec with protocol version $protocolVersion " +
      "and version $minecraftVersion",
  )
  logger.info("Loaded ${blockRegistry.size} blocks")
  logger.info("Server listening connections at $address")

  world = readAnvilWorld(blockRegistry, resource("world"))

  val gameServer = GameServer()

  while (true) {
    val session = Session(codec, server.accept())

    val exceptionHandler = CoroutineExceptionHandler { _, error ->
      when (error) {
        is AndesiteError -> logger.error(error::message)
        is ClosedReceiveChannelException, is ClosedWriteChannelException -> {
          // the connection just ended, no error to handle.
        }
        else -> logger.error(error) {
          "Error thrown while handling connection ${session.socket.remoteAddress}"
        }
      }

      runBlocking(Dispatchers.IO) {
        session.socket.close()
      }
    }

    launch(exceptionHandler) {
      try {
        val handshake = session.receivePacket<HandshakePacket>()

        when (handshake.nextState) {
          NextState.Status -> handleStatus(session, handshake)
          NextState.Login -> gameServer.handlePlay(session, handleLogin(session, handshake))
        }
      } catch (error: AndesiteError) {
        logger.error(error::message)
      } catch (error: Throwable) {
        if (session.socket.isClosed) {
          cancel("Connection closed", AndesiteError("Connection closed", error))
        }
      }
    }
  }
}
