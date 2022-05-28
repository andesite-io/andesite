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
import andesite.event.MinecraftEvent
import andesite.player.JavaPlayer
import andesite.player.PlayerQuitEvent
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
import io.ktor.utils.io.ClosedWriteChannelException
import java.net.InetSocketAddress
import kotlin.coroutines.CoroutineContext
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.benwoodworth.knbt.Nbt
import org.apache.logging.log4j.kotlin.Logging

internal class JavaMinecraftServer(
  context: CoroutineContext,
  hostname: String,
  port: Int,
  override val spawn: Location,
  override val motd: Motd,
  override val codec: MinecraftCodec,
  override val blockRegistry: BlockRegistry,
) : MinecraftServer,
  CoroutineScope by CoroutineScope(context + CoroutineName("java-minecraft-server")) {
  companion object : Logging

  override val players: MutableList<JavaPlayer> = mutableListOf()

  private val selector = ActorSelectorManager(Dispatchers.IO)
  private val address = InetSocketAddress(hostname, port)

  private val eventFlow = MutableSharedFlow<MinecraftEvent>()

  override fun eventFlow(): Flow<MinecraftEvent> {
    return eventFlow
  }

  override val nbt: Nbt get() = codec.configuration.nbt

  override val protocolVersion = codec.configuration.protocolVersion
  override val minecraftVersion = extractMinecraftVersion(codec.configuration.protocolVersion)

  internal val dimensionCodec = nbt.decodeRootTag<DimensionCodec>(resource("dimension_codec.nbt"))

  internal val dimension = nbt.decodeRootTag<Dimension>(resource("dimension.nbt"))

  private val sessionId = atomic(0)

  internal suspend fun publish(event: MinecraftEvent) {
    eventFlow.emit(event)
  }

  override fun listen(): Unit = runBlocking(coroutineContext) {
    logger.info("Starting andesite...")

    val server = aSocket(selector).tcp().bind(address)

    logger.info(
      "Set up minecraft codec with protocol version $protocolVersion " +
        "and version $minecraftVersion",
    )
    logger.info("Loaded ${blockRegistry.size} blocks")
    logger.info("Server listening connections at $address")

    while (true) {
      val nextId = sessionId.incrementAndGet()
      val session = Session(nextId, codec, server.accept())

      launch(CoroutineName("session-$nextId")) {
        try {
          val handshake = session.receivePacket<HandshakePacket>()

          when (handshake.nextState) {
            NextState.Status -> handleStatus(session, handshake)
            NextState.Login -> {
              val player = handleLogin(session, handshake)

              runCatching { handlePlay(session, player).join() }

              players.remove(player)
              publish(PlayerQuitEvent(player))
            }
          }
        } catch (error: Throwable) {
          when (error) {
            is AndesiteError -> logger.error(error::message)
            is ClosedReceiveChannelException, is ClosedWriteChannelException -> {}
            else -> logger.error(error) {
              "Error thrown while handling connection ${session.socket.remoteAddress}"
            }
          }

          withContext(Dispatchers.IO) {
            session.socket.close()
          }
        }
      }
    }
  }
}
