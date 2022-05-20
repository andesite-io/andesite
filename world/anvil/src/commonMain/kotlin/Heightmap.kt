/*
 *    Copyright 2022 Gabrielle Guimarães de Oliveira
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

package andesite.world.anvil

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
class Heightmap

@Serializable(HeightmapKeySerializer::class)
enum class HeightmapKind(val kind: String, val usage: HeightmapUsage) {
  WorldSurfaceWg("WORLD_SURFACE_WG", HeightmapUsage.LiveWorld),
  WorldSurface("WORLD_SURFACE", HeightmapUsage.Client),
  OceanFloorWg("OCEAN_FLOOR_WG", HeightmapUsage.LiveWorld),
  OceanFloor("OCEAN_FLOOR", HeightmapUsage.Client),
  MotionBlocking("MOTION_BLOCKING", HeightmapUsage.Client),
  MotionBlockingNoLeaves("MOTION_BLOCKING_NO_LEAVES", HeightmapUsage.Client),
}

enum class HeightmapUsage {
  WorldGen, Client, LiveWorld
}

object HeightmapKeySerializer : KSerializer<HeightmapKind> {
  override val descriptor: SerialDescriptor = HeightmapKeyDescriptor

  override fun serialize(encoder: Encoder, value: HeightmapKind) {
    encoder.encodeString(value.kind)
  }

  override fun deserialize(decoder: Decoder): HeightmapKind {
    val kind = decoder.decodeString()

    return HeightmapKind.values().find { it.kind == kind } ?: error("Unknown heightmap kind $kind")
  }

  object HeightmapKeyDescriptor : SerialDescriptor by String.serializer().descriptor {
    override val serialName: String = "HeightmapKey"
  }
}
