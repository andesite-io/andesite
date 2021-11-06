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

package com.gabrielleeg1.javarock.server.java

import com.gabrielleeg1.javarock.api.protocol.Codec
import com.gabrielleeg1.javarock.api.protocol.Packet
import com.gabrielleeg1.javarock.api.protocol.chat.Chat
import com.gabrielleeg1.javarock.api.protocol.java.JavaPacket
import com.gabrielleeg1.javarock.api.protocol.java.handshake.HandshakePacket
import com.gabrielleeg1.javarock.api.protocol.java.handshake.NextState
import com.gabrielleeg1.javarock.api.protocol.java.handshake.PingPacket
import com.gabrielleeg1.javarock.api.protocol.java.handshake.Players
import com.gabrielleeg1.javarock.api.protocol.java.handshake.Response
import com.gabrielleeg1.javarock.api.protocol.java.handshake.ResponsePacket
import com.gabrielleeg1.javarock.api.protocol.java.handshake.Version
import com.gabrielleeg1.javarock.api.protocol.readVarInt
import com.gabrielleeg1.javarock.api.protocol.writeVarInt
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.readPacket
import io.ktor.utils.io.writePacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KLogging
import mu.KotlinLogging
import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

private val context = Executors.newCachedThreadPool().asCoroutineDispatcher()

private val logger = KotlinLogging.logger { }

class Session(socket: Socket) {
  companion object : KLogging()

  val input = socket.openReadChannel()
  val output = socket.openWriteChannel()

  suspend fun acceptPacket(): JavaPacket {
    TODO()
  }

  suspend inline fun <reified T : JavaPacket> receivePacket(): T {
    val size = input.readVarInt()
    val packet = input.readPacket(size.toInt())

    val id = packet.readVarInt()

    logger.debug {
      "${T::class.simpleName} packet received with id [0x%02x] and size [$size]".format(id.toInt())
    }

    return codec(T::class).read(packet) as T
  }

  suspend fun sendPacket(packet: JavaPacket) {
    output.writePacket {
      val data = buildPacket {
        writeVarInt(packetId(packet::class))
        writePacket(codec(packet::class).write(packet))
      }

      logger.debug { "Packet sent with id [0x%02x]".format(packetId(packet::class)) }

      writeVarInt(data.remaining.toInt())
      writePacket(data)
    }
    output.flush()
  }

  internal fun <T : JavaPacket> packetId(packetClass: KClass<T>): Int {
    val annotation = packetClass.findAnnotation<Packet>()
      ?: error("Can not find Packet id annotation in packet ${packetClass.simpleName}")

    return annotation.id
  }

  @Suppress("UNCHECKED_CAST")
  @PublishedApi
  internal fun <T : Any> codec(packetClass: KClass<T>): Codec<Any> {
    val annotation = packetClass.findAnnotation<Packet>()
      ?: error("Can not find Packet id annotation in packet ${packetClass.simpleName}")

    if (annotation.codecClass != Codec::class) {
      return annotation.codecClass.objectInstance as? Codec<Any>
        ?: error("Can not find ${packetClass.simpleName}'s codec object instance")
    }

    TODO("get codec by current protocol")
  }
}

suspend fun main(): Unit = withContext(context) {
  val selector = ActorSelectorManager(Dispatchers.IO)
  val server = aSocket(selector).tcp().bind(hostname = "0.0.0.0", port = 25565)

  while (true) {
    val socket = server.accept()
    val session = Session(socket)

    launch {
      try {
        val handshake = session.receivePacket<HandshakePacket>()

        when (handshake.nextState) {
          NextState.Status -> handleLogin(session, handshake)
          NextState.Login -> TODO()
        }
      } catch (error: Throwable) {
        logger.error(error) { "Error thrown while handling connection ${socket.remoteAddress}" }
      }
    }
  }
}

private suspend fun handleLogin(session: Session, handshake: HandshakePacket) {
  session.sendPacket(
    ResponsePacket(
      Response(
        version = Version(name = "Javarock for 1.17", protocol = handshake.protocolVersion.toInt()),
        players = Players(max = 20, online = 0),
        description = Chat.of("&eHello, world"),
      ),
    ),
  )

  session.sendPacket(session.receivePacket<PingPacket>())
}
