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

package andesite.server.java.server

import andesite.AndesiteError
import andesite.player.JavaPlayer
import andesite.protocol.java.data.Dimension
import andesite.protocol.java.data.DimensionCodec
import andesite.protocol.java.handshake.HandshakePacket
import andesite.protocol.java.handshake.NextState
import andesite.protocol.resource
import andesite.protocol.serialization.MinecraftCodec
import andesite.protocol.serialization.extractMinecraftVersion
import andesite.server.MinecraftServer
import andesite.server.Motd
import andesite.server.java.decodeRootTag
import andesite.server.java.handlers.handleLogin
import andesite.server.java.handlers.handlePlay
import andesite.server.java.handlers.handleStatus
import andesite.server.java.player.Session
import andesite.server.java.player.receivePacket
import andesite.world.Location
import andesite.world.block.BlockRegistry
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.isClosed
import io.ktor.utils.io.ClosedWriteChannelException
import java.net.InetSocketAddress
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.benwoodworth.knbt.Nbt
import org.apache.logging.log4j.kotlin.Logging

internal class JavaMinecraftServer(
  scope: CoroutineScope,
  hostname: String,
  port: Int,
  override val spawn: Location,
  override val motd: Motd,
  override val codec: MinecraftCodec,
  override val blockRegistry: BlockRegistry,
) : MinecraftServer, CoroutineScope by scope {
  companion object : Logging

  override val players: MutableList<JavaPlayer> = mutableListOf()

  private val selector = ActorSelectorManager(Dispatchers.IO)
  private val address = InetSocketAddress(hostname, port)

  override val nbt: Nbt get() = codec.configuration.nbt

  override val protocolVersion = codec.configuration.protocolVersion
  override val minecraftVersion = extractMinecraftVersion(codec.configuration.protocolVersion)

  internal val dimensionCodec = nbt.decodeRootTag<DimensionCodec>(resource("dimension_codec.nbt"))

  internal val dimension = nbt.decodeRootTag<Dimension>(resource("dimension.nbt"))

  override suspend fun listen(): Unit = coroutineScope {
    logger.info("Starting andesite...")

    val server = aSocket(selector).tcp().bind(address)

    logger.info(
      "Set up minecraft codec with protocol version $protocolVersion " +
        "and version $minecraftVersion",
    )
    logger.info("Loaded ${blockRegistry.size} blocks")
    logger.info("Server listening connections at $address")

    while (true) {
      val session = Session(codec, server.accept())

      launch(createExceptionHandler(session)) {
        try {
          val handshake = session.receivePacket<HandshakePacket>()

          when (handshake.nextState) {
            NextState.Status -> handleStatus(session, handshake)
            NextState.Login -> {
              handleLogin(session, handshake)
                .also { player -> session.player = player }
                .let { player -> handlePlay(session, player) }
            }
          }
        } catch (error: AndesiteError) {
          logger.error(error::message)
        } catch (error: Throwable) {
          if (session.socket.isClosed) {
            cancel("Connection closed", PlayerQuitException(error))
          }
        }
      }
    }
  }

  private class PlayerQuitException(override val cause: Throwable) : RuntimeException()

  private fun createExceptionHandler(session: Session): CoroutineExceptionHandler =
    CoroutineExceptionHandler { _, error ->
      when (error) {
        is AndesiteError -> logger.error(error::message)
        is ClosedReceiveChannelException,
        is ClosedWriteChannelException,
        is PlayerQuitException,
        -> {
          val player = session.player
          if (player != null) {
            players.remove(player)
          }
        }
        else -> logger.error(error) {
          "Error thrown while handling connection ${session.socket.remoteAddress}"
        }
      }

      runBlocking(Dispatchers.IO) {
        session.socket.close()
      }
    }
}
