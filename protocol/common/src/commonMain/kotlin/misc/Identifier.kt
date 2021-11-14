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

package com.gabrielleeg1.javarock.api.protocol.misc

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(IdentifierSerializer::class)
class Identifier(private val fullPath: String) {
  val namespace: String = fullPath.substringBefore(':', "minecraft")
  val path: String = fullPath.substringAfter(':')
  
  constructor(namespace: String, path: String) : this("$namespace:$path")

  override fun toString(): String = fullPath
}

private object IdentifierSerializer : KSerializer<Identifier> {
  override val descriptor = PrimitiveSerialDescriptor(
    "com.gabrielleeg1.javarock.api.protocol.misc.Identifier",
    PrimitiveKind.STRING,
  )

  override fun deserialize(decoder: Decoder): Identifier {
    return Identifier(decoder.decodeString())
  }

  override fun serialize(encoder: Encoder, value: Identifier) {
    encoder.encodeString(value.toString())
  }
}
