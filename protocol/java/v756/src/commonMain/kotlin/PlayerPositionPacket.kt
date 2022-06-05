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

package andesite.protocol.java.v756

import andesite.protocol.ProtocolPacket
import andesite.protocol.java.JavaPacket
import andesite.world.Location
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PlayerPositionPacket")
@ProtocolPacket(0x11)
public data class PlayerPositionPacket(
  val x: Double,
  val feetY: Double,
  val z: Double,
  val onGround: Boolean,
) : JavaPacket, PositionMutatorPacket {
  override fun apply(location: Location): Location {
    return location.copy(x = x, y = feetY, z = z)
  }
}
