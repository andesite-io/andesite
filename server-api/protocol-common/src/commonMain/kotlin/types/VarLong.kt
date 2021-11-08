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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = VarLongSerializer::class)
@JvmInline
value class VarLong internal constructor(private val inner: Long) : Comparable<Number> {
  companion object {
    fun of(value: Long): VarLong {
      return VarLong(value)
    }
  }
  
  fun toLong(): Long = inner

  operator fun minus(value: VarLong): VarLong = VarLong(inner - value.inner)
  operator fun minus(value: Long): VarLong = VarLong(inner - value)

  operator fun plus(value: VarLong): VarLong = VarLong(inner + value.inner)
  operator fun plus(value: Long): VarLong = VarLong(inner + value)

  operator fun times(value: VarLong): VarLong = VarLong(inner * value.inner)
  operator fun times(value: Long): VarLong = VarLong(inner * value)

  operator fun div(value: VarLong): VarLong = VarLong(inner / value.inner)
  operator fun div(value: Long): VarLong = VarLong(inner / value)

  override fun compareTo(other: Number): Int = inner.compareTo(other.toLong())

  override fun toString(): String = inner.toString()
}

fun Decoder.decodeVarLong(): VarInt = decodeSerializableValue(VarInt.serializer())

fun Encoder.encodeVarLong(value: Long): Unit =
  encodeSerializableValue(VarLong.serializer(), VarLong(value))

fun Encoder.encodeVarLong(value: VarLong): Unit =
  encodeSerializableValue(VarLong.serializer(), value)

object VarLongSerializer : KSerializer<VarLong> {
  override val descriptor: SerialDescriptor
    get() = TODO("Not yet implemented")

  override fun deserialize(decoder: Decoder): VarLong {
    TODO("Not yet implemented")
  }

  override fun serialize(encoder: Encoder, value: VarLong) {
    TODO("Not yet implemented")
  }
}
