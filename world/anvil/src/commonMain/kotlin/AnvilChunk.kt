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

import com.gabrielleeg1.javarock.api.world.Chunk
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtTag

@Serializable
class AnvilChunk(
  val isLightOn: Boolean,
  @SerialName("xPos") val x: Int,
  @SerialName("zPos") val z: Int,
  @SerialName("LastUpdate") val lastUpdate: Long,
  @SerialName("InhabitedTime") val inhabitedTime: Long,
  @SerialName("Heightmaps") val heightmaps: NbtCompound,
  @SerialName("Biomes") val biomes: IntArray,
  @SerialName("TileEntities") val tileEntities: List<NbtTag>,
  @SerialName("TileTicks") val tileTicks: List<NbtTag>,
  @SerialName("Status") val status: String,
  @SerialName("Sections") override val sections: List<AnvilChunkSection> = emptyList(),
) : Chunk {
}
