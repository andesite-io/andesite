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

package com.gabrielleeg1.andesite.server.java.player

import com.gabrielleeg1.andesite.api.protocol.java.JavaPacket
import com.gabrielleeg1.andesite.api.protocol.readVarInt
import com.gabrielleeg1.andesite.api.protocol.writeVarInt
import io.klogging.Klogging
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.writeFully
import io.ktor.utils.io.readPacket
import io.ktor.utils.io.writePacket
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy

internal data class Session(val format: BinaryFormat, val socket: Socket) {
  companion object : Klogging

  val input = socket.openReadChannel()
  val output = socket.openWriteChannel()

  suspend fun acceptPacket(): JavaPacket {
    TODO()
  }

  suspend fun <T : JavaPacket> receivePacket(deserializer: DeserializationStrategy<T>): T {
    val name = deserializer.descriptor.serialName

    val size = input.readVarInt()
    val packet = input.readPacket(size.toInt())

    val id = packet.readVarInt().toInt()

    logger.debug("Packet `$name` received with id [0x%02x] and size [$size]".format(id))

    return format.decodeFromByteArray(deserializer, packet.readBytes())
  }

  suspend fun <T : JavaPacket> sendPacket(serializer: SerializationStrategy<T>, packet: T) {
    val packetName = serializer.descriptor.serialName
    val packetId = extractPacketId(packet::class)

    output.writePacket {
      val data = buildPacket {
        writeVarInt(packetId)
        writeFully(format.encodeToByteArray(serializer, packet))
      }

      logger.debug(
        "Packet `$packetName` sent with id [0x%02x] with size [${data.remaining}]".format(packetId)
      )

      writeVarInt(data.remaining.toInt())
      writePacket(data)
    }
    output.flush()
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
}
