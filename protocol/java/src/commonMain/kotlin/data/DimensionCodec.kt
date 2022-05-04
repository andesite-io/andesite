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

package andesite.protocol.java.data

import andesite.protocol.misc.Identifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("DimensionCodec")
data class DimensionCodec(
  @SerialName("minecraft:dimension_type")
  val dimension: Registry<Dimension>,

  @SerialName("minecraft:worldgen/biome")
  val worldgen: Registry<Worldgen>,
) {
  init {
    require(dimension.kind == "minecraft:dimension_type")
    require(worldgen.kind == "minecraft:worldgen/biome")
  }
}

@Serializable
@SerialName("Registry")
data class Registry<T : Any>(@SerialName("type") val kind: String, val value: List<Entry<T>>) {
  @Serializable
  @SerialName("Registry.Entry")
  data class Entry<T : Any>(val name: Identifier, val id: Int, val element: T)
}

