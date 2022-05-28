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

import andesite.on
import andesite.player.PlayerChatEvent
import andesite.player.PlayerJoinEvent
import andesite.player.PlayerQuitEvent
import andesite.protocol.java.v756.v756
import andesite.protocol.misc.Chat
import andesite.protocol.misc.ShowText
import andesite.protocol.misc.UuidSerializer
import andesite.protocol.resource
import andesite.protocol.serialization.MinecraftCodec
import andesite.server.java.server.createJavaServer
import andesite.world.Location
import andesite.world.anvil.readAnvilWorld
import andesite.world.block.readBlockRegistry
import java.lang.System.getSecurityManager
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import net.benwoodworth.knbt.Nbt
import net.benwoodworth.knbt.NbtCompression
import net.benwoodworth.knbt.NbtVariant

suspend fun main(): Unit = withContext(scope.coroutineContext + SupervisorJob()) {
  val server = createJavaServer(this) {
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

  val scope = Executors
    .newFixedThreadPool(8)
    .asCoroutineDispatcher()
    .let { CoroutineScope(it + SupervisorJob()) }

  server.on<PlayerJoinEvent>(scope) {
    server.players.forEach {
      it.sendMessage("{player} joined the server") {
        val player by placeholder(player.username) {
          hoverEvent = ShowText("@${player.username}")

          hex("#32a852")
        }
      }
    }
  }

  server.on<PlayerQuitEvent>(scope) {
    server.players.forEach {
      it.sendMessage("{player} left the server") {
        val player by placeholder(player.username) {
          hoverEvent = ShowText("@${player.username}")

          hex("#32a852")
        }
      }
    }
  }

  server.on<PlayerChatEvent>(scope) {
    server.players.forEach {
      it.sendMessage("<${player.username}> ${message.text}")
    }
  }

  server.listen()
}

private val context = Executors
  .newFixedThreadPool(4, AndesiteThreadFactory)
  .asCoroutineDispatcher()

private val scope = CoroutineScope(context)

private object AndesiteThreadFactory : ThreadFactory {
  const val NAME_PREFIX = "andesite-pool-"
  val threadNumber = AtomicInteger(0)
  val group: ThreadGroup = getSecurityManager()?.threadGroup ?: Thread.currentThread().threadGroup

  override fun newThread(runnable: Runnable): Thread {
    return Thread(group, runnable, NAME_PREFIX + threadNumber.incrementAndGet(), 0).apply {
      isDaemon = false
      priority = Thread.NORM_PRIORITY
    }
  }
}
