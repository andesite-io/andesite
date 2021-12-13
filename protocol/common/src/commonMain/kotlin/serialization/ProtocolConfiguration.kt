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

package com.gabrielleeg1.andesite.api.protocol.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.benwoodworth.knbt.Nbt
import net.benwoodworth.knbt.NbtCompression
import net.benwoodworth.knbt.NbtVariant

/**
 * Configuration for the serialization of the protocol.
 */
class ProtocolConfiguration(
  val protocolVersion: Int,
  val protocolVariant: ProtocolVariant = ProtocolVariant.Java,
  val serializersModule: SerializersModule = EmptySerializersModule,
  val nbt: Nbt = Nbt {
    variant = NbtVariant.Java
    compression = NbtCompression.None
  },
  val json: Json = Json {},
  val encryption: Boolean = false,
  var encodeDefaults: Boolean = false,
)

enum class ProtocolVariant {
  Java, Bedrock;
}
