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

package com.gabrielleeg1.javarock.api.world.anvil

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
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

internal fun readRegion(regionX: Int, regionZ: Int, bytes: ByteArray): AnvilRegion {
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

    val nbtCompression = NbtCompression.detect(bytes.drop(pos + 1).toByteArray())

    val nbt = Nbt {
      variant = NbtVariant.Java
      compression = nbtCompression
      ignoreUnknownKeys = true
    }

    nbt.decodeFromByteArray<RegionChunk>(bytes.drop(pos + 1).toByteArray())
      .level
  }
  
  return AnvilRegion(chunks.filterNotNull())
}

fun readAnvilWorld(file: File): AnvilWorld {
  val regions = file
    .resolve("region").listFiles()
    .orEmpty()
    .mapNotNull {
      val (_, regionX, regionZ) = it.name.split(".")
      val packet = it.readBytes()

      if (packet.isEmpty()) {
        null
      } else {
        readRegion(regionX.toInt(), regionZ.toInt(), packet)
      }
    }
    .toTypedArray()
  
  return AnvilWorld(regions)
}
