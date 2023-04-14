/*
 *    Copyright 2023 Gabrielle Guimarães de Oliveira
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

import andesite.world.Location
import andesite.world.World
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
import okio.BufferedSource
import okio.FileSystem
import okio.Path
import org.apache.logging.log4j.kotlin.logger

private val logger = logger("andesite.AnvilWorld")

public class AnvilWorld(public val nbt: Nbt, public val regionFiles: Map<String, Path>) : World {
  public companion object {
    /**
     * Reads an [AnvilWorld] with the [registry] and the world folder [folder].
     *
     * @param registry the [BlockRegistry] to use for the world
     * @param folder the world folder to read from
     * @return a new [AnvilWorld]
     */
    public fun of(registry: BlockRegistry, folder: Path): AnvilWorld {
      val nbt = Nbt {
        variant = NbtVariant.Java
        compression = NbtCompression.None
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
          contextual(AnvilChunkSectionSerializer(registry))
        }
      }

      val regionFiles = FileSystem.SYSTEM.list(folder.resolve("region"))

      val regions = regionFiles.associateBy { it.name.substringBeforeLast(".") }

      return AnvilWorld(nbt, regions)
    }

    private fun readChunk(regionFile: Path, nbt: Nbt, chunkX: Int, chunkZ: Int): AnvilChunk? =
      FileSystem.SYSTEM.read(regionFile) {
        // Read location entry from location table
        val locationTable = ByteArray(4096)
        read(locationTable)

        val pos = ((chunkX and 31) + (chunkZ and 31) * 32) * 4
        readChunk(this, locationTable, pos, nbt)
      }

    private fun readChunk(
      regionFile: BufferedSource,
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
      regionFile.skip((offset.toLong() - 1) * 4096)
      regionFile.read(regionChunkBytes)

      // Skipping chunk header
      regionChunkBytes = regionChunkBytes.drop(5).toByteArray()
      return Nbt(nbt) { compression = NbtCompression.detect(regionChunkBytes) }
        .decodeFromByteArray<RegionChunk>(regionChunkBytes)
        .level
    }
  }

  override fun getChunkAt(location: Location): AnvilChunk? {
    return getChunkAt(location.x.toInt(), location.y.toInt())
  }

  override fun getChunkAt(x: Int, z: Int): AnvilChunk? {
    val chunkX = x shr 4
    val chunkZ = z shr 4

    return readChunk(
      regionFiles["r.${chunkX shr 5}.${chunkZ shr 5}"]!!,
      nbt,
      chunkX,
      chunkZ,
    )
  }
}

@Serializable
@SerialName("")
internal data class RegionChunk(
  @SerialName("Level") val level: AnvilChunk,
  @SerialName("DataVersion") val dataVersion: Int,
)
