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
import com.gabrielleeg1.andesite.api.protocol.types.VarInt
import com.gabrielleeg1.andesite.api.world.anvil.AnvilChunk
import com.gabrielleeg1.andesite.api.world.anvil.HeightmapUsage
import io.klogging.config.ConfigDsl
import io.klogging.config.KloggingConfiguration
import io.klogging.config.LoggingConfig
import io.klogging.logger
import io.ktor.utils.io.core.BytePacketBuilder
import io.ktor.utils.io.core.readBytes
import kotlinx.serialization.encodeToString
import net.benwoodworth.knbt.StringifiedNbt
import net.benwoodworth.knbt.buildNbtCompound
import net.benwoodworth.knbt.encodeToNbtTag

@ConfigDsl
internal fun KloggingConfiguration.logging(vararg names: String, block: LoggingConfig.() -> Unit) {
  for (name in names) {
    logging {
      fromLoggerBase(name)
      block()
    }
  }
}

internal fun resource(path: String): String {
  return ClassLoader.getSystemResource(path)?.file ?: error("Can not find resource $path")
}

private val logger = logger("Utils")

private val sNbt = StringifiedNbt { prettyPrint = true }

internal suspend fun AnvilChunk.toPacket(): ChunkDataPacket {
  val heightmaps = heightmaps
    .filterKeys { it.usage == HeightmapUsage.Client }
    .mapKeys { it.key.kind }
    .let { nbt.encodeToNbtTag(it) }

  val buf = BytePacketBuilder()
  val data = buf.build().readBytes()
  val primaryBitmask = extractChunkData(buf)

  println("Sending chunk packet ${calculateChunkSize()}")
  print("  Heightmaps    : ")
  println(sNbt.encodeToString(heightmaps).split("\n").joinToString("\n  "))
  println("  Biomes length : ${biomes.size}")
  println("  Data length   : ${data.size}")
  println("  ")

  return ChunkDataPacket(
    x, z,
    primaryBitmask.toLongArray(),
    buildNbtCompound { put("", heightmaps) },
    biomes.map(::VarInt),
    data,
    emptyList(), // TODO
  )
}
