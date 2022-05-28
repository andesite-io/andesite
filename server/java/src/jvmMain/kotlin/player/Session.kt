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

@file:OptIn(
  ExperimentalSerializationApi::class, ExperimentalSerializationApi::class,
  ExperimentalSerializationApi::class,
)

package andesite.server.java.player

import andesite.andesiteError
import andesite.protocol.ProtocolPacket
import andesite.protocol.extractPacketId
import andesite.protocol.java.JavaPacket
import andesite.protocol.readVarInt
import andesite.protocol.serialization.MinecraftCodec
import andesite.protocol.serialization.findAnnotation
import andesite.protocol.writeVarInt
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.writeFully
import io.ktor.utils.io.readPacket
import io.ktor.utils.io.writePacket
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer
import org.apache.logging.log4j.kotlin.Logging

internal data class Session(val codec: MinecraftCodec, val socket: Socket) :
  CoroutineScope by CoroutineScope(CoroutineName("session-${socket.remoteAddress}")) {

  companion object : Logging

  val input = socket.openReadChannel()
  val output = socket.openWriteChannel()

  val inboundPacketFlow = MutableSharedFlow<JavaPacket>()

  suspend fun acceptPacket(): JavaPacket? {
    val size = input.readVarInt()
    val packet = input.readPacket(size.toInt())

    val id = packet.readVarInt().toInt()

    val type = codec.configuration.packetRegistry[id]
      ?: return null.also {
        logger.trace { "Could not find a serializer for packet: 0x%02x".format(id) }
      }

    val deserializer = codec.serializersModule
      .serializer(type)
      .apply { checkId(id) }

    val name = deserializer.descriptor.serialName

    logger.trace { "Packet `$name` received with id [0x%02x] and size [$size]".format(id) }

    val javaPacket = codec.decodeFromByteArray(deserializer, packet.readBytes())
      ?: andesiteError("Decoded packet `$name` is null")

    return javaPacket as? JavaPacket
      ?: andesiteError(
        "Packet `$name` must be a JavaPacket, " +
          "received ${javaPacket::class.simpleName}",
      )
  }

  suspend fun <T : JavaPacket> receivePacket(deserializer: DeserializationStrategy<T>): T {
    val name = deserializer.descriptor.serialName

    val size = input.readVarInt()
    val packet = input.readPacket(size.toInt())

    val id = packet.readVarInt().toInt()

    deserializer.checkId(id)

    logger.trace { "Packet `$name` received with id [0x%02x] and size [$size]".format(id) }

    return codec.decodeFromByteArray(deserializer, packet.readBytes())
  }

  @Suppress("BlockingMethodInNonBlockingContext")
  suspend fun <T : JavaPacket> sendPacket(serializer: SerializationStrategy<T>, packet: T) {
    val packetName = serializer.descriptor.serialName
    val packetId = extractPacketId(serializer.descriptor)

    output.writePacket {
      val data = buildPacket {
        writeVarInt(packetId)
        writeFully(codec.encodeToByteArray(serializer, packet))
      }

      logger.trace {
        "Packet `$packetName` sent with id [0x%02x] with size [${data.remaining}]".format(packetId)
      }

      writeVarInt(data.remaining.toInt())
      writePacket(data)
    }

    output.flush()
  }

  fun close(cause: CancellationException? = null) {
    socket.close()
    cancel(cause)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Session

    if (socket != other.socket) return false

    return true
  }

  override fun hashCode(): Int {
    return socket.hashCode()
  }

  override fun toString(): String {
    return "Session(remoteAddress=${socket.remoteAddress})"
  }

  private fun <A> DeserializationStrategy<A>.checkId(id: Int) {
    val name = descriptor.serialName

    val realId = descriptor.findAnnotation<ProtocolPacket>()?.id
      ?: andesiteError("Packet `$name` is not annotated with @ProtocolPacket")

    if (id != realId) {
      andesiteError("Packet `$name` received with id [0x%02x] but expected [0x%02x]", id, realId)
    }
  }
}
