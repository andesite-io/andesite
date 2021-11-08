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

package com.gabrielleeg1.javarock.api.protocol.types

import com.gabrielleeg1.javarock.api.protocol.readVarInt
import com.gabrielleeg1.javarock.api.protocol.writeVarInt
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = VarIntSerializer::class)
@JvmInline
value class VarInt internal constructor(private val inner: Int) : Comparable<Number> {
  companion object {
    fun of(value: Int): VarInt {
      return VarInt(value)
    }
  }
  
  fun toInt(): Int = inner

  operator fun minus(value: VarInt): VarInt = VarInt(inner - value.inner)
  operator fun minus(value: Int): VarInt = VarInt(inner - value)

  operator fun plus(value: VarInt): VarInt = VarInt(inner + value.inner)
  operator fun plus(value: Int): VarInt = VarInt(inner + value)

  operator fun times(value: VarInt): VarInt = VarInt(inner * value.inner)
  operator fun times(value: Int): VarInt = VarInt(inner * value)

  override fun compareTo(other: Number): Int = inner.compareTo(other.toInt())

  override fun toString(): String = inner.toString()
}

fun Decoder.decodeVarInt(): VarInt = decodeSerializableValue(VarInt.serializer())

fun Encoder.encodeVarInt(value: Int): Unit =
  encodeSerializableValue(VarInt.serializer(), VarInt(value))

fun Encoder.encodeVarInt(value: VarInt): Unit =
  encodeSerializableValue(VarInt.serializer(), value)

object VarIntSerializer : KSerializer<VarInt> {
  override val descriptor = PrimitiveSerialDescriptor("VarInt", PrimitiveKind.INT)

  override fun deserialize(decoder: Decoder): VarInt {
    return readVarInt(decoder::decodeByte)
  }

  override fun serialize(encoder: Encoder, value: VarInt) {
    writeVarInt(value, encoder::encodeByte)
  }
}
