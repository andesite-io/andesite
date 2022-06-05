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

import andesite.world.BitSet
import andesite.world.Chunk
import io.ktor.utils.io.core.Output
import io.ktor.utils.io.core.writePacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtTag

@Serializable
public class AnvilChunk(
  public val isLightOn: Boolean,
  @SerialName("xPos") public val x: Int,
  @SerialName("zPos") public val z: Int,
  @SerialName("LastUpdate") public val lastUpdate: Long,
  @SerialName("InhabitedTime") public val inhabitedTime: Long,
  @SerialName("Heightmaps") public val heightmaps: Map<HeightmapKind, NbtTag>,
  @SerialName("Biomes") public val biomes: IntArray,
  @SerialName("TileEntities") public val tileEntities: List<NbtTag>,
  @SerialName("TileTicks") public val tileTicks: List<NbtTag>,
  @SerialName("Status") public val status: String,
  @SerialName("Sections") override val sections: List<@Contextual AnvilChunkSection> = emptyList(),
) : Chunk {
  public fun calculateChunkSize(): Int {
    var i = 0
    var j = 0

    val k = sections.size
    while (j < k) {
      val section = sections[j]
      if (!section.isEmpty()) {
        i += section.serializedSize
      }
      ++j
    }

    return i
  }

  public fun extractChunkData(buf: Output): BitSet {
    val bitset = BitSet()

    var i = 0
    val j = sections.size
    while (i < j) {
      val section = sections[i]
      section.blockStates.recount()
      if (!section.isEmpty()) {
        bitset.set(i)
        buf.writePacket(section.writeToNetwork())
      }
      ++i
    }

    return bitset
  }
}
