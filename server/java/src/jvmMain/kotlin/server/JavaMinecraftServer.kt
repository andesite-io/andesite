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

package andesite.java.server

import andesite.AndesiteError
import andesite.command.MinecraftKomandaRoot
import andesite.event.MinecraftEvent
import andesite.java.decodeRootTag
import andesite.java.player.Session
import andesite.java.player.receivePacket
import andesite.java.processLogin
import andesite.java.processPlay
import andesite.java.processStatus
import andesite.komanda.KomandaRoot
import andesite.player.JavaPlayer
import andesite.player.PlayerQuitEvent
import andesite.protocol.java.data.Dimension
import andesite.protocol.java.data.DimensionCodec
import andesite.protocol.java.handshake.HandshakePacket
import andesite.protocol.java.handshake.NextState
import andesite.protocol.resource
import andesite.protocol.serialization.MinecraftCodec
import andesite.protocol.serialization.extractMinecraftVersion
import andesite.server.Messageable
import andesite.server.MinecraftServer
import andesite.server.Motd
import andesite.world.Location
import andesite.world.block.BlockRegistry
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.ClosedWriteChannelException
import java.net.InetSocketAddress
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import net.benwoodworth.knbt.Nbt

internal class JavaMinecraftServer(
  context: CoroutineContext,
  hostname: String,
  port: Int,
  override val spawn: Location,
  override val motd: Motd,
  override val codec: MinecraftCodec,
  override val blockRegistry: BlockRegistry,
) :
  MinecraftServer,
  CoroutineScope by CoroutineScope(context + CoroutineName("andesite")),
  KomandaRoot<Messageable> by MinecraftKomandaRoot() {
  private val playersMutex = Mutex()
  private val playersMut: MutableList<JavaPlayer> = mutableListOf()
  override val players: List<JavaPlayer> get() = playersMut

  private val selector = ActorSelectorManager(Dispatchers.IO)
  private val address = InetSocketAddress(hostname, port)

  private val eventFlow = MutableSharedFlow<MinecraftEvent>()

  override fun eventFlow(): Flow<MinecraftEvent> {
    return eventFlow
  }

  override val nbt: Nbt get() = codec.configuration.nbt
  override val json: Json get() = codec.configuration.json

  override val protocolVersion = codec.configuration.protocolVersion
  override val minecraftVersion = extractMinecraftVersion(codec.configuration.protocolVersion)

  internal val dimensionCodec = nbt.decodeRootTag<DimensionCodec>(resource("dimension_codec.nbt"))

  internal val dimension = nbt.decodeRootTag<Dimension>(resource("dimension.nbt"))

  internal suspend fun addPlayer(player: JavaPlayer) {
    playersMutex.withLock { playersMut.add(player) }
  }

  internal suspend fun removePlayer(player: JavaPlayer) {
    playersMutex.withLock { playersMut.remove(player) }
  }

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
      val session = Session(codec, server.accept())

      launch(CoroutineName("session-${session.socket.remoteAddress}")) {
        try {
          val handshake = session.receivePacket<HandshakePacket>()

          when (handshake.nextState) {
            NextState.Status -> processStatus(session, handshake)
            NextState.Login -> {
              val player = processLogin(session, handshake)

              runCatching { processPlay(session, player).join() }

              removePlayer(player)
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
