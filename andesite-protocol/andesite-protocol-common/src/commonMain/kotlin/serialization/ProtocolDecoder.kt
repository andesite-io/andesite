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

@file:OptIn(ExperimentalSerializationApi::class, OkioApi::class, ExperimentalUnsignedTypes::class)

package andesite.protocol.serialization

import andesite.protocol.ProtocolEnum
import andesite.protocol.ProtocolJson
import andesite.protocol.ProtocolNbt
import andesite.protocol.ProtocolString
import andesite.protocol.ProtocolValue
import andesite.protocol.ProtocolVariant
import andesite.protocol.Variant
import andesite.protocol.readString
import andesite.protocol.readVarInt
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readDouble
import io.ktor.utils.io.core.readInt
import io.ktor.utils.io.core.readLong
import io.ktor.utils.io.core.readShort
import io.ktor.utils.io.core.readUByte
import io.ktor.utils.io.core.readUInt
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import net.benwoodworth.knbt.Nbt
import net.benwoodworth.knbt.OkioApi

/**
 * A [Decoder] that deserializes a [ProtocolValue] from a [ByteReadPacket].
 */
public interface ProtocolDecoder : Decoder, CompositeDecoder {
  public val nbt: Nbt
  public val json: Json

  public fun <T : Any> decodeNbt(deserializer: DeserializationStrategy<T>): T

  public fun <T> decodeJson(deserializer: DeserializationStrategy<T>): T
}

public fun Decoder.asProtocolDecoder(): ProtocolDecoder {
  return this as? ProtocolDecoder
    ?: error(
      "This deserializer can be used only with Protocol format. " +
        "Expected Decoder to be ProtocolDecoder, got ${this::class}",
    )
}

internal class ProtocolDecoderImpl(
  val packet: ByteReadPacket,
  val configuration: ProtocolConfiguration,
) : ProtocolDecoder {
  private var currentIndex = 0

  override val nbt: Nbt = configuration.nbt
  override val json: Json = configuration.json
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
  override fun decodeString(): String = packet.readString()

  override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
    return if (enumDescriptor.hasAnnotation<ProtocolEnum>()) {
      val variant = enumDescriptor
        .annotations
        .filterIsInstance<ProtocolVariant>()
        .singleOrNull()?.kind ?: Variant.VarInt

      val value = decodeType(variant)

      enumDescriptor.elementDescriptors
        .withIndex()
        .singleOrNull { (i) ->
          value == enumDescriptor
            .getElementAnnotations(i)
            .filterIsInstance<ProtocolValue>()
            .singleOrNull()
            ?.value
        }
        ?.index
        ?: error(
          "Can not decode ${enumDescriptor.serialName} cause value index: " +
            "$value does not exist",
        )
    } else {
      enumDescriptor.getElementIndex(packet.readString())
    }
  }

  @ExperimentalSerializationApi
  override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder = this

  override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String = when {
    descriptor.getElementAnnotations(index).filterIsInstance<ProtocolString>().isNotEmpty() -> {
      val string = descriptor
        .getElementAnnotations(index)
        .filterIsInstance<ProtocolString>()
        .first()

      val value = decodeString()

      require(value.length <= string.max) {
        "String length ${value.length} is greater than max length ${string.max}"
      }

      value
    }

    else -> decodeString()
  }

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

    if (packet.isEmpty) {
      currentIndex = 0
      return DECODE_DONE
    }

    if (i >= descriptor.elementsCount) {
      currentIndex = 0
      return DECODE_DONE
    }

    return i
  }

  override fun <T> decodeSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    deserializer: DeserializationStrategy<T>,
    previousValue: T?,
  ): T {
    return when {
      descriptor.getElementAnnotations(index).filterIsInstance<ProtocolJson>().isNotEmpty() -> {
        configuration.json.decodeFromString(deserializer, packet.readString())
      }

      descriptor.getElementAnnotations(index).filterIsInstance<ProtocolNbt>().isNotEmpty() -> {
        configuration.nbt.decodeFromSource(deserializer, InputSource(packet))
      }

      else -> deserializer.deserialize(ProtocolDecoderImpl(packet, configuration))
    }
  }

  override fun decodeSequentially(): Boolean = true

  override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
    val variant = descriptor.annotations
      .filterIsInstance<ProtocolVariant>()
      .singleOrNull()?.kind ?: Variant.VarInt

    return decodeType(variant)
  }

  @ExperimentalSerializationApi
  override fun <T : Any> decodeNullableSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    deserializer: DeserializationStrategy<T?>,
    previousValue: T?,
  ): T? {
    if (descriptor.isElementOptional(index) && !configuration.encodeDefaults) {
      return previousValue
    }

    error("Can not decode null in Minecraft Protocol format.")
  }

  override fun endStructure(descriptor: SerialDescriptor) {
    currentIndex = 0
  }

  @ExperimentalSerializationApi
  override fun decodeNull(): Nothing = error("Can not decode null in Minecraft Protocol format.")

  @ExperimentalSerializationApi
  override fun decodeNotNullMark(): Boolean = true

  override fun <T : Any> decodeNbt(deserializer: DeserializationStrategy<T>): T {
    return configuration.nbt.decodeFromSource(deserializer, InputSource(packet))
  }

  override fun <T> decodeJson(deserializer: DeserializationStrategy<T>): T {
    return configuration.json.decodeFromString(deserializer, packet.readString())
  }

  private fun decodeType(variant: Variant): Int {
    return when (variant) {
      Variant.Byte -> packet.readByte().toInt()
      Variant.UByte -> packet.readUByte().toInt()
      Variant.Int -> packet.readInt()
      Variant.UInt -> packet.readUInt().toInt()
      Variant.VarInt -> packet.readVarInt().toInt()
      Variant.VarLong -> TODO()
    }
  }
}
