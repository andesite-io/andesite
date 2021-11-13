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

package com.gabrielleeg1.javarock.api.protocol.java.play

import com.gabrielleeg1.javarock.api.protocol.Packet
import com.gabrielleeg1.javarock.api.protocol.java.JavaPacket
import com.gabrielleeg1.javarock.api.protocol.types.VarInt
import kotlinx.serialization.Serializable

@Packet(0x38)
@Serializable
data class PlayerPositionAndLookPacket(
  val x: Double,
  val y: Double,
  val z: Double,
  val yaw: Float,
  val pitch: Float,
  val flags: Byte,
  val teleportId: VarInt,
  val dismountVehicle: Boolean,
) : JavaPacket
