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

package andesite.protocol.java.data

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Worldgen")
public data class Worldgen(
  val depth: Float,
  val temperature: Float,
  val scale: Float,
  val downfall: Float,
  val category: String,
  val effects: Effects,
  val particle: Particle? = null,

  @EncodeDefault
  val precipitation: String = "none",

  @SerialName("temperature_modifier")
  val temperatureModifier: String? = null,
) {
  init {
    require(precipitation in listOf("rain", "snow", "none")) {
      "precipitation must be one of rain, snow or none"
    }
    require(depth in -1.8f..1.5f) { "depth must be between -1.8 and 1.5" }
    require(temperature in -0.5f..2f) { "temperature must be between -0.5 and 2" }
    require(scale in 0f..1.225f) { "scale must be between 0 and 1.225" }
    require(downfall in 0f..1f) { "downfall must be between 0 and 1" }
  }

  @Serializable
  @SerialName("Worldgen.Effect")
  public data class Effects(
    val music: Music? = null,

    @SerialName("sky_color")
    val skyColor: Int,

    @SerialName("water_color")
    val waterColor: Int,

    @SerialName("water_fog_color")
    val waterFogColor: Int,

    @SerialName("fog_color")
    val fogColor: Int,

    @SerialName("foliage_color")
    val foliageColor: Int? = null,

    @SerialName("grass_color")
    val grassColor: Int? = null,

    @SerialName("grass_color_modifier")
    val grassColorModifier: String? = null,

    @SerialName("ambient_sound")
    val ambientSound: String? = null,

    @SerialName("additions_sound")
    val additionsSound: AdditionsSound? = null,

    @SerialName("mood_sound")
    val moodSound: MoodSound? = null,
  )

  @Serializable
  @SerialName("Worldgen.AdditionsSound")
  public data class AdditionsSound(
    val sound: String,

    @SerialName("tick_chance")
    val tickChance: Float,
  )

  @Serializable
  @SerialName("Worldgen.MoodSound")
  public data class MoodSound(
    val sound: String,
    val offset: Float,

    @SerialName("tick_delay")
    val tickDelay: Int,

    @SerialName("block_search_extent")
    val blockSearchExtent: Int,
  )

  @Serializable
  @SerialName("Worldgen.Music")
  public data class Music(
    val sound: String,

    @SerialName("replace_current_music")
    val replaceCurrentMusic: Boolean,

    @SerialName("max_delay")
    val maxDelay: Int,

    @SerialName("min_delay")
    val minDelay: Int,
  )

  @Serializable
  @SerialName("Worldgen.Particle")
  public data class Particle(
    val probability: Float,
    val options: ParticleOptions,
  )

  @Serializable
  @SerialName("Worldgen.ParticleOptions")
  public data class ParticleOptions(@SerialName("type") val kind: String)
}
