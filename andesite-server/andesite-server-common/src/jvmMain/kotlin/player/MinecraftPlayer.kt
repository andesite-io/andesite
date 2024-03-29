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

package andesite.player

import andesite.event.EventHolder
import andesite.protocol.java.JavaPacket
import andesite.protocol.misc.Uuid
import andesite.server.Messageable
import andesite.shared.AndesiteInternalAPI
import andesite.world.Location
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer
import org.apache.logging.log4j.kotlin.Logging

/** Represents a generic Minecraft player. */
public sealed interface MinecraftPlayer : EventHolder<PlayerEvent>, Logging, Messageable {
  public val id: Uuid
  public val protocol: Int
  public val username: String
  public var location: Location
    @AndesiteInternalAPI set
}

/** Represents a Java Edition Minecraft player. */
public interface JavaPlayer : MinecraftPlayer {
  public suspend fun <A : JavaPacket> sendPacket(serializer: SerializationStrategy<A>, packet: A)
}

public suspend inline fun <reified A : JavaPacket> JavaPlayer.sendPacket(packet: A) {
  sendPacket(serializer(), packet)
}

/** Represents a Bedrock Edition Minecraft player. */
public interface BedrockPlayer : MinecraftPlayer
