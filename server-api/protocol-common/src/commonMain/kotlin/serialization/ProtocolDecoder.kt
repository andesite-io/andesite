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

import com.gabrielleeg1.javarock.api.protocol.ProtocolEnum
import com.gabrielleeg1.javarock.api.protocol.readString
import com.gabrielleeg1.javarock.api.protocol.readVarInt
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.readDouble
import io.ktor.utils.io.core.readInt
import io.ktor.utils.io.core.readLong
import io.ktor.utils.io.core.readShort
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder

interface ProtocolDecoder : Decoder, CompositeDecoder

fun Decoder.asProtocolDecoder(): ProtocolDecoder {
  return this as? ProtocolDecoder
    ?: error("This deserializer can be used only with Protocol format. Expected Decoder to be ProtocolDecoder, got ${this::class}")
}

internal class ProtocolDecoderImpl(
  val packet: ByteReadPacket,
  val configuration: ProtocolConfiguration,
) : ProtocolDecoder {
  private var currentIndex = 0
  override val serializersModule = configuration.serializersModule

  override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = this

  override fun decodeBoolean(): Boolean = packet.readByte().toInt() != 0
  override fun decodeByte(): Byte = packet.readByte()
  override fun decodeChar(): Char = packet.readByte().toInt().toChar()
  override fun decodeDouble(): Double = packet.readDouble()
  override fun decodeFloat(): Float = packet.readDouble().toFloat()
  override fun decodeInt(): Int = packet.readInt()
  override fun decodeLong(): Long = packet.readLong()
  override fun decodeShort(): Short = packet.readShort()
  override fun decodeString(): String = packet.readString(String.hashCode())

  override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
    val value = packet.readVarInt().toInt()

    return if (enumDescriptor.annotations.filterIsInstance<ProtocolEnum>().isNotEmpty()) {
      enumDescriptor.elementDescriptors
        .withIndex()
        .singleOrNull { (i) ->
          value == enumDescriptor.getElementAnnotations(i)
            .filterIsInstance<ProtocolEnum.Entry>()
            .singleOrNull()
            ?.value
        }
        ?.index
        ?: error("Can not decode ${enumDescriptor.serialName} cause value index: $value does not exist")
    } else {
      enumDescriptor.getElementIndex(packet.readString())
    }
  }

  @ExperimentalSerializationApi
  override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder = this

  override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String =
    decodeString()

  override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
    decodeBoolean()

  override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
    decodeDouble()

  override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte = decodeByte()
  override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char = decodeChar()
  override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float = decodeFloat()
  override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int = decodeInt()
  override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long = decodeLong()
  override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short = decodeShort()

  @ExperimentalSerializationApi
  override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
    return decodeInline(descriptor)
  }

  override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
    val i = currentIndex
    currentIndex++

    if (i >= descriptor.elementsCount) {
      currentIndex = 0
      return -1
    }

    return i
  }

  override fun <T> decodeSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    deserializer: DeserializationStrategy<T>,
    previousValue: T?
  ): T {
    return deserializer.deserialize(this)
  }

  @ExperimentalSerializationApi
  override fun <T : Any> decodeNullableSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    deserializer: DeserializationStrategy<T?>,
    previousValue: T?
  ): T? {
    error("Can not decode null in Minecraft Protocol format.")
  }

  override fun endStructure(descriptor: SerialDescriptor) {
  }

  @ExperimentalSerializationApi
  override fun decodeNull(): Nothing = error("Can not decode null in Minecraft Protocol format.")

  @ExperimentalSerializationApi
  override fun decodeNotNullMark(): Boolean = true
}
