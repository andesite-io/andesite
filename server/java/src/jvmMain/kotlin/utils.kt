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

package andesite.server.java

import andesite.protocol.java.v756.ChunkDataPacket
import andesite.protocol.types.VarInt
import andesite.world.anvil.AnvilChunk
import andesite.world.anvil.HeightmapUsage
import io.ktor.utils.io.core.BytePacketBuilder
import io.ktor.utils.io.core.readBytes
import net.benwoodworth.knbt.StringifiedNbt
import net.benwoodworth.knbt.buildNbtCompound
import net.benwoodworth.knbt.encodeToNbtTag
import org.apache.logging.log4j.kotlin.logger

private val logger = logger("andesite.Utils")

private val sNbt = StringifiedNbt { prettyPrint = true }

internal suspend fun AnvilChunk.toPacket(): ChunkDataPacket {
  val heightmaps = heightmaps
    .filterKeys { it.usage == HeightmapUsage.Client }
    .mapKeys { it.key.kind }
    .let { nbt.encodeToNbtTag(it) }

  val buf = BytePacketBuilder()
  val primaryBitmask = extractChunkData(buf)
  val data = buf.build().readBytes()

  return ChunkDataPacket(
    x, z,
    primaryBitmask.toLongArray(),
    buildNbtCompound { put("", heightmaps) },
    biomes.map(::VarInt),
    data,
    emptyList(), // TODO
  )
}
