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

import com.gabrielleeg1.javarock.api.world.ChunkSection
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound

@Serializable
class AnvilChunkSection(
  @SerialName("Y") val y: Byte,
  @SerialName("SkyLight") val skyLight: ByteArray = ByteArray(1024),
  @SerialName("BlockStates") val blockStates: LongArray = LongArray(1024),
  @SerialName("Palette") val palette: List<NbtCompound> = emptyList(),
) : ChunkSection
