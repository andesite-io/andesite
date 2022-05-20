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

package andesite.protocol

import andesite.protocol.types.VarInt
import com.benasher44.uuid.Uuid
import io.ktor.utils.io.core.BytePacketBuilder
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.readLong
import io.ktor.utils.io.core.toByteArray
import io.ktor.utils.io.core.writeFully
import io.ktor.utils.io.core.writeLong
import kotlin.experimental.and

public fun BytePacketBuilder.writeVarInt(varint: Int): Unit = writeVarInt(VarInt(varint))

public fun BytePacketBuilder.writeVarInt(varint: VarInt): Unit =
  writeVarInt(varint) { writeByte(it) }

public fun ByteReadPacket.readVarInt(): VarInt = readVarInt { readByte() }

public fun VarInt.countVarInt(): Int {
  var count = 0
  writeVarInt(this) { count++ }
  return count
}

public fun Int.countVarInt(): Int {
  var count = 0
  writeVarInt(VarInt(this)) { count++ }
  return count
}

internal inline fun writeVarInt(varint: VarInt, writeByte: (Byte) -> Unit) {
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

internal inline fun readVarInt(readByte: () -> Byte): VarInt {
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

public fun BytePacketBuilder.writeUuid(uuid: Uuid) {
  writeLong(uuid.mostSignificantBits)
  writeLong(uuid.leastSignificantBits)
}

public fun ByteReadPacket.readUuid(): Uuid {
  val mostSignificantBits = readLong()
  val leastSignificantBits = readLong()

  return Uuid(mostSignificantBits, leastSignificantBits)
}

public fun BytePacketBuilder.writeString(string: String) {
  val bytes = string.toByteArray()
  writeVarInt(bytes.size)
  writeFully(bytes)
}

public fun ByteReadPacket.readString(max: Int = -1): String {
  val size = readVarInt().toInt()
  return when {
    max == -1 -> readBytes(size).decodeToString()
    size > max -> error("The string size is larger than the supported: $max")
    else -> readBytes(size).decodeToString()
  }
}
