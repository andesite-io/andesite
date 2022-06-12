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

import andesite.protocol.misc.Chat
import andesite.protocol.serialization.MinecraftCodec
import andesite.server.MinecraftServer
import andesite.server.MinecraftServerBuilder
import andesite.server.Motd
import andesite.server.MotdBuilder
import andesite.shared.AndesiteProperties
import andesite.world.Location
import andesite.world.block.BlockRegistry
import kotlin.coroutines.CoroutineContext

fun createJavaServer(
  context: CoroutineContext,
  builder: MinecraftServerBuilder.() -> Unit,
): MinecraftServer {
  return MinecraftServerBuilderImpl(context).apply(builder).build()
}

private class MotdBuilderImpl : MotdBuilder {
  override var version: String by AndesiteProperties.builder()
  override var maxPlayers: Int by AndesiteProperties.builder()
  override var text: Chat by AndesiteProperties.builder()

  fun build(): Motd {
    return Motd(version, maxPlayers, text)
  }
}

private class MinecraftServerBuilderImpl(val context: CoroutineContext) : MinecraftServerBuilder {
  override var hostname: String by AndesiteProperties.builder()
  override var port: Int by AndesiteProperties.builder()
  override var spawn: Location by AndesiteProperties.builder()
  override var blockRegistry: BlockRegistry by AndesiteProperties.builder()
  override var codec: MinecraftCodec by AndesiteProperties.builder()
  var motd: Motd by AndesiteProperties.builder()

  override fun motd(builder: MotdBuilder.() -> Unit) {
    motd = MotdBuilderImpl().apply(builder).build()
  }

  fun build(): MinecraftServer {
    return JavaMinecraftServer(context, hostname, port, spawn, motd, codec, blockRegistry)
  }
}
