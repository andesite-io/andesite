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

import andesite.protocol.readVarInt
import andesite.protocol.writeVarInt
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline

/**
 * A variable-length integer.
 *
 * @param int The value of the integer.
 */
@Serializable(with = VarIntSerializer::class)
@SerialName("VarInt")
@JvmInline
public value class VarInt(private val int: Int) : Comparable<Number> {
  public fun toInt(): Int = int

  public operator fun minus(value: VarInt): VarInt = VarInt(int - value.int)
  public operator fun minus(value: Int): VarInt = VarInt(int - value)

  public operator fun plus(value: VarInt): VarInt = VarInt(int + value.int)
  public operator fun plus(value: Int): VarInt = VarInt(int + value)

  public operator fun times(value: VarInt): VarInt = VarInt(int * value.int)
  public operator fun times(value: Int): VarInt = VarInt(int * value)

  override fun compareTo(other: Number): Int = int.compareTo(other.toInt())

  override fun toString(): String = int.toString()
}

public fun Decoder.decodeVarInt(): VarInt = decodeSerializableValue(VarInt.serializer())

public fun Encoder.encodeVarInt(value: Int): Unit =
  encodeSerializableValue(VarInt.serializer(), VarInt(value))

public fun Encoder.encodeVarInt(value: VarInt): Unit =
  encodeSerializableValue(VarInt.serializer(), value)

public object VarIntSerializer : KSerializer<VarInt> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("VarInt", PrimitiveKind.INT)

  override fun deserialize(decoder: Decoder): VarInt {
    return readVarInt(decoder::decodeByte)
  }

  override fun serialize(encoder: Encoder, value: VarInt) {
    writeVarInt(value, encoder::encodeByte)
  }
}
