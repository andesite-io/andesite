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

import andesite.world.block.BlockRegistry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import net.benwoodworth.knbt.Nbt
import net.benwoodworth.knbt.NbtCompression
import net.benwoodworth.knbt.NbtVariant
import net.benwoodworth.knbt.detect
import org.apache.logging.log4j.kotlin.logger
import java.io.File
import java.io.RandomAccessFile

private val logger = logger("andesite.AnvilWorld")

/**
 * Reads an [AnvilWorld] with the [registry] and the world folder [folder].
 *
 * @param registry the [BlockRegistry] to use for the world
 * @param folder the world folder to read from
 * @return a new [AnvilWorld]
 */
public fun readAnvilWorld(registry: BlockRegistry, folder: File): AnvilWorld {
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

    readRegion(file, nbt)
  }

  logger.info("Finish loading world `${folder.name}`")

  return AnvilWorld(regions.filterNotNull().toTypedArray())
}

internal fun readRegion(regionFile: File, nbt: Nbt): AnvilRegion? {
  val raf = RandomAccessFile(regionFile, "r")

  if (raf.length() == 0L) return null

  val locationTable = ByteArray(4096)
  raf.read(locationTable)

  val chunks = List(1024) { i ->
    readChunk(raf, locationTable, i * 4, nbt)
  }.filterNotNull()

  raf.close()
  return AnvilRegion(regionFile.nameWithoutExtension, chunks)
}

internal fun readChunk(
  regionFile: RandomAccessFile,
  locationTable: ByteArray,
  locationTablePos: Int,
  nbt: Nbt,
): AnvilChunk? {
  val offset = locationTable[locationTablePos + 0].toInt() and 0xff shl 16 or
    (locationTable[locationTablePos + 1].toInt() and 0xff shl 8) or
    (locationTable[locationTablePos + 2].toInt() and 0xff)
  val size = locationTable[locationTablePos + 3].toInt() and 0xFF

  if (offset == 0 && size == 0) {
    // Chunk not generated yet
    return null
  }

  // Read chunk data from file
  var regionChunkBytes = ByteArray(size * 4096)
  regionFile.seek(offset.toLong() * 4096)
  regionFile.read(regionChunkBytes)

  // No one knows why, but we had to skip 5 bytes...
  regionChunkBytes = regionChunkBytes.drop(5).toByteArray()
  return Nbt(nbt) { compression = NbtCompression.detect(regionChunkBytes) }
    .decodeFromByteArray<RegionChunk>(regionChunkBytes)
    .level
}

@Serializable
@SerialName("")
internal data class RegionChunk(
  @SerialName("Level") val level: AnvilChunk,
  @SerialName("DataVersion") val dataVersion: Int,
)
