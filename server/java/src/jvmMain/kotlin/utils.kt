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

@file:OptIn(ExperimentalSerializationApi::class)

package andesite.server.java

import andesite.protocol.java.v756.ChunkDataPacket
import andesite.protocol.types.VarInt
import andesite.server.MinecraftServer
import andesite.world.Chunk
import andesite.world.anvil.AnvilChunk
import andesite.world.anvil.HeightmapUsage
import io.ktor.utils.io.core.BytePacketBuilder
import io.ktor.utils.io.core.readBytes
import java.io.File
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.descriptors.serialDescriptor
import net.benwoodworth.knbt.Nbt
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.buildNbtCompound
import net.benwoodworth.knbt.decodeFromNbtTag
import net.benwoodworth.knbt.encodeToNbtTag
import net.benwoodworth.knbt.nbtCompound
import org.apache.logging.log4j.kotlin.logger

private val logger = logger("andesite.Utils")

inline fun <reified T : Any> Nbt.decodeRootTag(file: File): T {
  val descriptor = serialDescriptor<T>()

  return decodeFromNbtTag(
    buildNbtCompound {
      val root = decodeFromByteArray<NbtCompound>(file.readBytes())
      val content = root[""]?.nbtCompound ?: error("Could not find content for nbt file $file")

      put(descriptor.serialName, content)
    },
  )
}

internal suspend fun MinecraftServer.convertChunk(chunk: Chunk): ChunkDataPacket {
  require(chunk is AnvilChunk)

  val heightmaps = chunk.heightmaps
    .filterKeys { it.usage == HeightmapUsage.Client }
    .mapKeys { it.key.kind }
    .let { nbt.encodeToNbtTag(it) }

  val buf = BytePacketBuilder()
  val primaryBitmask = chunk.extractChunkData(buf)
  val data = buf.build().readBytes()

  return ChunkDataPacket(
    chunk.x,
    chunk.z,
    primaryBitmask.toLongArray(),
    buildNbtCompound { put("", heightmaps) },
    chunk.biomes.map(::VarInt),
    data,
    emptyList(), // TODO
  )
}
