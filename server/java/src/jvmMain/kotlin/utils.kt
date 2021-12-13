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

package com.gabrielleeg1.andesite.server.java

import com.gabrielleeg1.andesite.api.protocol.java.play.ChunkDataPacket
import com.gabrielleeg1.andesite.api.world.anvil.AnvilChunk
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.writeFully
import net.benwoodworth.knbt.buildNbtCompound
import java.util.BitSet

internal fun AnvilChunk.toPacket(): ChunkDataPacket {
  val primaryBitmask = BitSet()
  val data = buildPacket {
    for (i in sections.indices) {
      primaryBitmask.set(i)
      writeFully(sections[i].writeToNetwork().readBytes())
    }
  }.readBytes()
  
  val heightmaps = buildNbtCompound { put("", heightmaps) }
  
  println("Sending chunk packet")
  println("Primary bitmask: $primaryBitmask")
  println("Heightmaps:      $heightmaps")
  println("Biomes length:   ${biomes.size}")
  println("Biomes:          $biomes")
  println("Data length:     ${data.size}")
  println("Data:            $data")

  return ChunkDataPacket(
    x, z,
    primaryBitmask.toLongArray(),
    heightmaps,
    biomes,
    data,
    emptyList(), // TODO
  )
}
