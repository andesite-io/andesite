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

import com.gabrielleeg1.andesite.api.world.Chunk
import io.ktor.utils.io.core.Output
import io.ktor.utils.io.core.writePacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtTag
import java.util.BitSet

@Serializable
class AnvilChunk(
  val isLightOn: Boolean,
  @SerialName("xPos") val x: Int,
  @SerialName("zPos") val z: Int,
  @SerialName("LastUpdate") val lastUpdate: Long,
  @SerialName("InhabitedTime") val inhabitedTime: Long,
  @SerialName("Heightmaps") val heightmaps: Map<HeightmapKind, NbtTag>,
  @SerialName("Biomes") val biomes: IntArray,
  @SerialName("TileEntities") val tileEntities: List<NbtTag>,
  @SerialName("TileTicks") val tileTicks: List<NbtTag>,
  @SerialName("Status") val status: String,
  @SerialName("Sections") override val sections: List<@Contextual AnvilChunkSection> = emptyList(),
) : Chunk {
  fun calculateChunkSize(): Int {
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

  fun extractChunkData(buf: Output): BitSet {
    val bitset = BitSet()

    var i = 0
    val j = sections.size
    while (i < j) {
      val section = sections[i]
      if (!section.isEmpty()) {
        bitset.set(i)
        buf.writePacket(section.writeToNetwork())
      }
      ++i
    }
    
    return bitset
  }
}
