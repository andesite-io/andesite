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

@file:OptIn(ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)

package andesite.protocol.serialization

import andesite.protocol.ProtocolEnum
import andesite.protocol.ProtocolJson
import andesite.protocol.ProtocolNbt
import andesite.protocol.ProtocolString
import andesite.protocol.ProtocolValue
import andesite.protocol.ProtocolVariant
import andesite.protocol.Variant
import andesite.protocol.writeString
import andesite.protocol.writeVarInt
import io.ktor.utils.io.core.BytePacketBuilder
import io.ktor.utils.io.core.writeDouble
import io.ktor.utils.io.core.writeFloat
import io.ktor.utils.io.core.writeFully
import io.ktor.utils.io.core.writeInt
import io.ktor.utils.io.core.writeLong
import io.ktor.utils.io.core.writeShort
import io.ktor.utils.io.core.writeUByte
import io.ktor.utils.io.core.writeUInt
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import net.benwoodworth.knbt.Nbt

interface ProtocolEncoder : Encoder, CompositeEncoder {
  val nbt: Nbt
  val json: Json
  
  fun <T : Any> encodeNbt(serializer: SerializationStrategy<T>, value: T)
  
  fun <T> encodeJson(serializer: SerializationStrategy<T>, value: T)
}

fun Encoder.asProtocolEncoder(): ProtocolEncoder {
  return this as? ProtocolEncoder
    ?: error("This serializer can be used only with Protocol format. Expected Encoder to be ProtocolEncoder, got ${this::class}")
}

internal class ProtocolEncoderImpl(
  val builder: BytePacketBuilder,
  val configuration: ProtocolConfiguration,
) : ProtocolEncoder {
  override val nbt: Nbt = configuration.nbt
  override val json: Json = configuration.json
  override val serializersModule = configuration.serializersModule

  override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = this

  override fun encodeBoolean(value: Boolean): Unit = builder.writeByte(if (value) 1 else 0)
  override fun encodeByte(value: Byte): Unit = builder.writeByte(value)
  override fun encodeChar(value: Char): Unit = builder.writeByte(value.code.toByte())
  override fun encodeDouble(value: Double): Unit = builder.writeDouble(value)
  override fun encodeFloat(value: Float): Unit = builder.writeFloat(value)
  override fun encodeInt(value: Int): Unit = builder.writeInt(value)
  override fun encodeLong(value: Long): Unit = builder.writeLong(value)
  override fun encodeShort(value: Short): Unit = builder.writeShort(value)
  override fun encodeString(value: String): Unit = builder.writeString(value)

  override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
    if (enumDescriptor.hasAnnotation<ProtocolEnum>()) {
      val variant = enumDescriptor
        .annotations
        .filterIsInstance<ProtocolVariant>()
        .singleOrNull()?.kind ?: Variant.VarInt

      val value = enumDescriptor
        .getElementAnnotations(index)
        .filterIsInstance<ProtocolValue>()
        .singleOrNull()
        ?.value
        ?: error("Can not encode enum ${enumDescriptor.serialName} index: 0 cause it does not have the @ProtocolValue annotation")

      encodeType(variant, value)
    } else {
      builder.writeString(enumDescriptor.getElementName(index))
    }
  }

  @ExperimentalSerializationApi
  override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder = this

  override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
    encodeBoolean(value)
  }

  override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
    encodeByte(value)
  }

  override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
    encodeChar(value)
  }

  override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
    encodeDouble(value)
  }

  override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
    encodeFloat(value)
  }

  override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
    encodeInt(value)
  }

  override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
    encodeLong(value)
  }

  override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
    encodeShort(value)
  }

  override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
    when {
      descriptor.getElementAnnotations(index).filterIsInstance<ProtocolString>().isNotEmpty() -> {
        val string = descriptor
          .getElementAnnotations(index)
          .filterIsInstance<ProtocolString>()
          .first()

        require(value.length <= string.max) { "String length ${value.length} is greater than max length ${string.max}" }
      }
      else -> encodeString(value)
    }
  }

  @ExperimentalSerializationApi
  override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
    return encodeInline(descriptor)
  }

  @ExperimentalSerializationApi
  override fun <T : Any> encodeNullableSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    serializer: SerializationStrategy<T>,
    value: T?,
  ) {
    if (descriptor.isElementOptional(index) && !configuration.encodeDefaults) {
      return
    }

    error("Can not encode null in Minecraft Protocol format.")
  }

  override fun <T> encodeSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    serializer: SerializationStrategy<T>,
    value: T,
  ) {
    try {
      when {
        descriptor.getElementAnnotations(index).filterIsInstance<ProtocolJson>().isNotEmpty() -> {
          builder.writeString(configuration.json.encodeToString(serializer, value))
        }
        descriptor.getElementAnnotations(index).filterIsInstance<ProtocolNbt>().isNotEmpty() -> {
          builder.writeFully(configuration.nbt.encodeToByteArray(serializer, value))
        }
        serializer.descriptor.kind == StructureKind.LIST -> {
          val listValue = when (value) {
            is Collection<*> -> value
            is Array<*> -> value.toList()
            is ByteArray -> value.toList()
            is CharArray -> value.toList()
            is ShortArray -> value.toList()
            is IntArray -> value.toList()
            is LongArray -> value.toList()
            is FloatArray -> value.toList()
            is DoubleArray -> value.toList()
            is BooleanArray -> value.toList()
            else -> error("Can not encode list of descriptor ${serializer.descriptor.serialName}")
          }

          val variant = descriptor
            .getElementAnnotations(index)
            .filterIsInstance<ProtocolVariant>()
            .singleOrNull()?.kind ?: Variant.VarInt

          encodeType(variant, listValue.size)
          serializer.serialize(this, value)
        }
        else -> serializer.serialize(this, value)
      }
    } catch (exception: SerializationException) {
      throw SerializationException(
        "${exception::class.simpleName} while encoding ${serializer.descriptor.serialName} and index $index",
        exception,
      )
    }
  }

  @ExperimentalSerializationApi
  override fun encodeNull(): Unit = error("Can not encode null in Minecraft Protocol format.")

  override fun <T : Any> encodeNbt(serializer: SerializationStrategy<T>, value: T) {
    builder.writeFully(configuration.nbt.encodeToByteArray(serializer, value))
  }

  override fun <T> encodeJson(serializer: SerializationStrategy<T>, value: T) {
    builder.writeString(configuration.json.encodeToString(serializer, value))
  }

  override fun endStructure(descriptor: SerialDescriptor) {
  }

  private fun encodeType(variant: Variant, value: Int) {
    when (variant) {
      Variant.Byte -> builder.writeByte(value.toByte())
      Variant.UByte -> builder.writeUByte(value.toUByte())
      Variant.Int -> builder.writeInt(value)
      Variant.UInt -> builder.writeUInt(value.toUInt())
      Variant.VarInt -> builder.writeVarInt(value)
      Variant.VarLong -> TODO()
    }
  }
}
