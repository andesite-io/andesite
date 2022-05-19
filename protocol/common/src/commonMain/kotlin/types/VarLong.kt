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

package andesite.protocol.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

/**
 * A variable-length long.
 *
 * @param long The value of the long.
 */
@Serializable(with = VarLongSerializer::class)
@SerialName("VarLong")
@JvmInline
public value class VarLong(private val long: Long) : Comparable<Number> {
  public fun toLong(): Long = long

  public operator fun minus(value: VarLong): VarLong = VarLong(long - value.long)
  public operator fun minus(value: Long): VarLong = VarLong(long - value)

  public operator fun plus(value: VarLong): VarLong = VarLong(long + value.long)
  public operator fun plus(value: Long): VarLong = VarLong(long + value)

  public operator fun times(value: VarLong): VarLong = VarLong(long * value.long)
  public operator fun times(value: Long): VarLong = VarLong(long * value)

  public operator fun div(value: VarLong): VarLong = VarLong(long / value.long)
  public operator fun div(value: Long): VarLong = VarLong(long / value)

  override fun compareTo(other: Number): Int = long.compareTo(other.toLong())

  override fun toString(): String = long.toString()
}

public fun Decoder.decodeVarLong(): VarInt = decodeSerializableValue(VarInt.serializer())

public fun Encoder.encodeVarLong(value: Long): Unit =
  encodeSerializableValue(VarLong.serializer(), VarLong(value))

public fun Encoder.encodeVarLong(value: VarLong): Unit =
  encodeSerializableValue(VarLong.serializer(), value)

public object VarLongSerializer : KSerializer<VarLong> {
  override val descriptor: SerialDescriptor
    get() = TODO("Not yet implemented")

  override fun deserialize(decoder: Decoder): VarLong {
    TODO("Not yet implemented")
  }

  override fun serialize(encoder: Encoder, value: VarLong) {
    TODO("Not yet implemented")
  }
}
