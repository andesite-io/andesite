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
@SerialName("Dimension")
public data class Dimension(
  val effects: Identifier,
  val infiniburn: Identifier,
  val natural: Boolean,

  @SerialName("piglin_safe")
  val piglinSafe: Boolean,

  @SerialName("ambient_light")
  val ambientLight: Float,

  @SerialName("fixed_time")
  val fixedTime: Long? = null,

  @SerialName("respawn_anchor_works")
  val respawnAnchorWorks: Boolean,

  @SerialName("has_skylight")
  val hasSkylight: Boolean,

  @SerialName("has_raids")
  val hasRaids: Boolean,

  @SerialName("has_ceiling")
  val hasCeiling: Boolean,

  @SerialName("bed_works")
  val bedWorks: Boolean,

  @SerialName("min_y")
  val minY: Int,

  @SerialName("height")
  val height: Int,

  @SerialName("logical_height")
  val logicalHeight: Short,

  @SerialName("coordinate_scale")
  val coordinateScale: Float,

  @SerialName("ultrawarm")
  val ultrawarm: Boolean,
) {
  init {
    require(ambientLight in 0f..1f) { "ambientLight must be between 0 and 1" }
  }
}
