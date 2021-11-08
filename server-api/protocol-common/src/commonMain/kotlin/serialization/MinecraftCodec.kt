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

package com.gabrielleeg1.javarock.api.protocol.serialization

import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readBytes
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import net.benwoodworth.knbt.Nbt

class MinecraftCodec(val configuration: ProtocolConfiguration) : BinaryFormat {
  override val serializersModule = configuration.serializersModule
  override fun <T> decodeFromByteArray(
    deserializer: DeserializationStrategy<T>,
    bytes: ByteArray
  ): T {
    return ProtocolDecoderImpl(ByteReadPacket(bytes), configuration)
      .decodeSerializableValue(deserializer)
  }

  override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
    return buildPacket {
      ProtocolEncoderImpl(this, configuration).encodeSerializableValue(serializer, value)
    }.readBytes()
  }
}

val DefaultProtocolConfiguration = ProtocolConfiguration(protocolVersion = -1)

fun MinecraftCodec(
  from: ProtocolConfiguration = DefaultProtocolConfiguration,
  builder: MinecraftCodecBuilder.() -> Unit,
): MinecraftCodec {
  return MinecraftCodecBuilder(from).apply(builder).build()
}

class MinecraftCodecBuilder(configuration: ProtocolConfiguration) {
  var protocolVersion: Int = configuration.protocolVersion
  var protocolVariant: ProtocolVariant = configuration.protocolVariant
  var json: Json = configuration.json
  var nbt: Nbt = configuration.nbt
  var serializersModule: SerializersModule = configuration.serializersModule
  var encryption: Boolean = configuration.encryption

  internal fun build(): MinecraftCodec {
    require(protocolVersion != -1) { "protocolVersion must be set" }

    return MinecraftCodec(
      ProtocolConfiguration(
        protocolVersion,
        protocolVariant,
        serializersModule,
        nbt,
        json,
        encryption,
      ),
    )
  }
}
