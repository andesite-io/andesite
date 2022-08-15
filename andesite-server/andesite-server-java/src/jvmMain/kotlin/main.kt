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

package andesite.java

import andesite.event.on
import andesite.java.server.createJavaServer
import andesite.player.PlayerChatEvent
import andesite.player.PlayerJoinEvent
import andesite.player.PlayerQuitEvent
import andesite.protocol.java.v756.v756
import andesite.protocol.misc.Chat
import andesite.protocol.misc.ShowText
import andesite.protocol.misc.UuidSerializer
import andesite.protocol.resource
import andesite.protocol.serialization.MinecraftCodec
import andesite.server.MinecraftServer
import andesite.server.broadcast
import andesite.world.Location
import andesite.world.anvil.readAnvilWorld
import andesite.world.block.readBlockRegistry
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import net.benwoodworth.knbt.Nbt
import net.benwoodworth.knbt.NbtCompression
import net.benwoodworth.knbt.NbtVariant

internal fun main() {
  System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)

  val server = createServer()

//  server.command("hello") {
//    pattern("send|to <target:player>") {
//      onPlayerExecution {
//        val target: MinecraftPlayer by arguments
//
//        target.sendMessage("Hello! (from ${sender.username})")
//      }
//    }
//  }

  server.on<PlayerJoinEvent> {
    server.broadcast("{player} joined the server") {
      val player by placeholder(player.username) {
        hoverEvent = ShowText("@${player.username}")

        hex("32a852")
      }
    }
  }

  server.on<PlayerQuitEvent> {
    server.broadcast("{player} left the server") {
      val player by placeholder(player.username) {
        hoverEvent = ShowText("@${player.username}")

        hex("32a852")
      }
    }
  }

  server.on<PlayerChatEvent> {
    server.broadcast("<${player.username}> ${message.text}")
  }

  server.listen()
}

private fun createServer(): MinecraftServer {
  return createJavaServer(SupervisorJob()) {
    blockRegistry = resource("v756")
      .resolve("blocks.json")
      .readText()
      .let(::readBlockRegistry)

    codec = MinecraftCodec.v756 {
      nbt = Nbt {
        variant = NbtVariant.Java
        compression = NbtCompression.None
        ignoreUnknownKeys = true
      }

      json = Json {
        prettyPrint = true
      }

      serializersModule = SerializersModule {
        contextual(UuidSerializer)
      }
    }

    hostname = "127.0.0.1"
    port = 25565
    spawn = Location(0.0, 10.0, 0.0, 0f, 0f, readAnvilWorld(blockRegistry, resource("world")))

    motd {
      maxPlayers = 20
      version = "Andesite for 1.17.1"
      text = Chat.of("&7A Minecraft Server")
    }
  }
}
