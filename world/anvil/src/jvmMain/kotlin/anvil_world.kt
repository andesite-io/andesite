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

package andesite.world.anvil

import andesite.world.anvil.block.BlockRegistry
import io.klogging.noCoLogger
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

private val logger = noCoLogger("andesite.AnvilWorld")

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

fun readAnvilWorld(registry: BlockRegistry, folder: File): AnvilWorld {
  logger.info("Loading world `${folder.name}`")

  val nbt = Nbt {
    variant = NbtVariant.Java
    compression = NbtCompression.None
    ignoreUnknownKeys = true
    serializersModule = SerializersModule {
      contextual(AnvilChunkSectionSerializer(registry))
    }
  }

  val fileRegions = folder.resolve("region").listFiles().orEmpty()

  val regions = fileRegions.mapIndexed { i, file ->
    val percentage = (i.toFloat() / fileRegions.size * 100).toInt()
    logger.info("Preparing region [$percentage%]")
    
    val bytes = file.readBytes()

    if (bytes.isEmpty()) {
      null
    } else {
      readRegion(file.nameWithoutExtension, nbt, bytes)
    }
  }

  logger.info("Finish loading world `${folder.name}`")

  return AnvilWorld(regions.filterNotNull().toTypedArray())
}
