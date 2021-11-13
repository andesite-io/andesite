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

package com.gabrielleeg1.javarock.server.java

import com.gabrielleeg1.javarock.api.protocol.java.play.ChunkDataPacket
import com.gabrielleeg1.javarock.api.protocol.writeVarInt
import com.gabrielleeg1.javarock.api.world.anvil.AnvilChunk
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.writeFully
import io.ktor.utils.io.core.writeShort
import io.ktor.utils.io.core.writeUByte
import kotlinx.serialization.encodeToByteArray

internal fun AnvilChunk.toPacket(): ChunkDataPacket {
  val data = buildPacket {
    for (section in sections) {
      val blocks = section.blockStates.toList()
        .map(Long::toInt)
        .mapNotNull(section.palette::getOrNull)

      writeShort(blocks.size.toShort())
      writeUByte(4.toUByte())

      writeVarInt(section.palette.size)
      for (item in blocks) {
        writeFully(nbt.encodeToByteArray(item))
      }

      writeVarInt(section.blockStates.size)
      writeFully(section.blockStates)
    }
  }.readBytes()

  return ChunkDataPacket(
    x, z,
    LongArray(sections.size) { 1 },
    heightmaps,
    biomes,
    data,
    tileEntities,
  )
}
