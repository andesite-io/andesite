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

package andesite.server

import andesite.event.EventHolder
import andesite.event.MinecraftEvent
import andesite.komanda.KomandaRoot
import andesite.player.MinecraftPlayer
import andesite.protocol.serialization.MinecraftCodec
import andesite.world.Location
import andesite.world.block.BlockRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import net.benwoodworth.knbt.Nbt
import org.apache.logging.log4j.kotlin.Logging

public interface MinecraftServer :
  CoroutineScope,
  EventHolder<MinecraftEvent>,
  Logging,
  KomandaRoot<Messageable> {
  public val codec: MinecraftCodec
  public val protocolVersion: Int
  public val minecraftVersion: String
  public val players: List<MinecraftPlayer>
  public val motd: Motd
  public val spawn: Location
  public val blockRegistry: BlockRegistry
  public val nbt: Nbt
  public val json: Json

  public fun listen()
}

public interface MinecraftServerBuilder {
  public var hostname: String
  public var port: Int
  public var spawn: Location
  public var blockRegistry: BlockRegistry
  public var codec: MinecraftCodec

  public fun motd(builder: MotdBuilder.() -> Unit)
}
