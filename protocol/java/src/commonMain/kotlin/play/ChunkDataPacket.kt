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

package com.gabrielleeg1.andesite.api.protocol.java.play

import com.gabrielleeg1.andesite.api.protocol.ProtocolPacket
import com.gabrielleeg1.andesite.api.protocol.ProtocolNbt
import com.gabrielleeg1.andesite.api.protocol.java.JavaPacket
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtTag

@Serializable
@ProtocolPacket(0x22)
data class ChunkDataPacket(
  val chunkX: Int,
  val chunkZ: Int,
  val primaryBitmask: LongArray,
  @ProtocolNbt
  val heightmaps: NbtCompound,
  val biomes: IntArray,
  val data: ByteArray,
//  todo support only on list field @ProtocolNbt
  val blockEntities: List<NbtTag>,
) : JavaPacket {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ChunkDataPacket

    if (chunkX != other.chunkX) return false
    if (chunkZ != other.chunkZ) return false
    if (!primaryBitmask.contentEquals(other.primaryBitmask)) return false
    if (heightmaps != other.heightmaps) return false
    if (!biomes.contentEquals(other.biomes)) return false
    if (!data.contentEquals(other.data)) return false
    if (blockEntities != other.blockEntities) return false

    return true
  }

  override fun hashCode(): Int {
    var result = chunkX
    result = 31 * result + chunkZ
    result = 31 * result + primaryBitmask.contentHashCode()
    result = 31 * result + heightmaps.hashCode()
    result = 31 * result + biomes.contentHashCode()
    result = 31 * result + data.contentHashCode()
    result = 31 * result + blockEntities.hashCode()
    return result
  }
}
