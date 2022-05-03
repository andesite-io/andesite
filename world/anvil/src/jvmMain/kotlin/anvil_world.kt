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

package com.gabrielleeg1.andesite.api.world.anvil

import com.gabrielleeg1.andesite.api.world.anvil.block.GlobalPalette
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import net.benwoodworth.knbt.Nbt
import net.benwoodworth.knbt.NbtCompression
import net.benwoodworth.knbt.NbtVariant
import net.benwoodworth.knbt.detect
import java.io.File

@Serializable
@SerialName("")
internal data class RegionChunk(
  @SerialName("Level") val level: AnvilChunk,
  @SerialName("DataVersion") val dataVersion: Int,
)

internal fun readRegion(name: String, nbt: Nbt, bytes: ByteArray): AnvilRegion {
  var pos: Int
  val chunks = List(1024) { i ->
    pos = i * 4
    val offset = (bytes[pos].toInt() shl 16) or
      ((bytes[pos + 1].toInt() and 0xff) shl 8) or
      (bytes[pos + 2].toInt() and 0xff)

    if (bytes[pos + 3] == 0.toByte()) {
      return@List null
    }

    pos = 4096 + i * 4
    bytes[pos + 4] // timestamp

    pos = 4096 * offset + 4

    val regionChunkBytes = bytes.drop(pos + 1).toByteArray()

    Nbt(nbt) { compression = NbtCompression.detect(regionChunkBytes) }
      .decodeFromByteArray<RegionChunk>(regionChunkBytes)
      .level
  }

  return AnvilRegion(name, chunks.filterNotNull())
}

fun readAnvilWorld(globalPalette: GlobalPalette, file: File): AnvilWorld {
  val nbt = Nbt {
    variant = NbtVariant.Java
    compression = NbtCompression.None
    ignoreUnknownKeys = true
    serializersModule = SerializersModule {
      contextual(AnvilChunkSectionSerializer(globalPalette))
    }
  }

  val regions = file
    .resolve("region").listFiles()
    .orEmpty()
    .mapNotNull {
      val bytes = it.readBytes()

      if (bytes.isEmpty()) {
        null
      } else {
        readRegion(it.nameWithoutExtension, nbt, bytes)
      }
    }
    .toTypedArray()

  return AnvilWorld(regions)
}
