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

package andesite.protocol.serialization

import kotlin.reflect.KType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.benwoodworth.knbt.Nbt
import net.benwoodworth.knbt.NbtCompression
import net.benwoodworth.knbt.NbtVariant

/**
 * Configuration for the serialization of the protocol.
 *
 * @param protocolVersion the version of the protocol to use.
 * @param protocolVariant the variant of the protocol to use.
 * @param serializersModule the serializers module to use.
 * @param packetRegistry the packet registry to use.
 * @param nbt the NBT configuration to use.
 * @param json the JSON configuration to use.
 * @param encryption the encryption configuration to use.
 * @param encodeDefaults whether to encode default values.
 */
public data class ProtocolConfiguration(
  val protocolVersion: Int,
  val protocolVariant: ProtocolVariant = ProtocolVariant.Java,
  val serializersModule: SerializersModule = EmptySerializersModule,
  val packetRegistry: Map<Int, KType> = emptyMap(),
  val nbt: Nbt = Nbt {
    variant = NbtVariant.Java
    compression = NbtCompression.None
  },
  val json: Json = Json,
  val encryption: Boolean = false,
  var encodeDefaults: Boolean = false,
)

/**
 * Represents the variant of the Minecraft protocol to use.
 */
public enum class ProtocolVariant {
  Java, Bedrock;
}

/**
 * Extracts the Minecraft version from [protocolVersion].
 *
 * @param protocolVersion the protocol version to extract the version from.
 * @return the Minecraft version.
 */
public fun extractMinecraftVersion(protocolVersion: Int): String = when (protocolVersion) {
  758 -> "1.18.1"
  757 -> "1.18"
  756 -> "1.17.1"
  755 -> "1.17"
  754 -> "1.16.4|1.16.5"
  753 -> "1.16.3"
  752 -> "1.16.3-rc1"
  751 -> "1.16.2"
  750 -> "1.16.2-rc2"
  749 -> "1.16.2-rc1"
  748 -> "1.16.2-pre3"
  747 -> "1.16.2-pre2"
  746 -> "1.16.2-pre1"
  744 -> "1.16.2-pre1"
  743 -> "20w30a"
  741 -> "20w29a"
  740 -> "20w28a"
  738 -> "20w27a"
  736 -> "1.16.1"
  735 -> "1.16"
  else -> error("Could not found minecraft version for protocol version $protocolVersion")
}
