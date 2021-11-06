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

package com.gabrielleeg1.javarock.api.protocol

import com.gabrielleeg1.javarock.api.protocol.types.VarInt
import io.ktor.utils.io.core.BytePacketBuilder
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.writeFully
import kotlin.experimental.and

fun BytePacketBuilder.writeVarInt(varint: Int): Unit = writeVarInt(VarInt(varint))

fun BytePacketBuilder.writeVarInt(varint: VarInt) {
  var value = varint.toInt()

  while (true) {
    if ((value and 0xFFFFFF80.toInt()) == 0) {
      writeByte(value.toByte())
      return
    }

    writeByte(((value and 0x7F) or 0x80).toByte())
    value = value ushr 7
  }
}

fun ByteReadPacket.readVarInt(): VarInt {
  var offset = 0
  var value = 0L
  var byte: Byte

  do {
    if (offset == 35) error("VarInt too long")

    byte = readByte()
    value = value or ((byte.toLong() and 0x7FL) shl offset)

    offset += 7
  } while ((byte and 0x80.toByte()) != 0.toByte())

  return VarInt(value.toInt())
}

fun BytePacketBuilder.writeString(string: String) {
  val bytes = string.toByteArray()
  writeVarInt(bytes.size)
  writeFully(bytes)
}

fun ByteReadPacket.readString(max: Int): String {
  val size = readVarInt()
  return when {
    size > max -> error("The string size is larger than the supported: $max")
    else -> readBytes(size.toInt()).decodeToString()
  }
}
