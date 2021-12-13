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

package tests

import com.gabrielleeg1.andesite.api.protocol.java.play.GameMode
import com.gabrielleeg1.andesite.api.protocol.java.play.JoinGamePacket
import com.gabrielleeg1.andesite.api.protocol.java.play.PreviousGameMode
import com.gabrielleeg1.andesite.api.protocol.serialization.MinecraftCodec
import com.gabrielleeg1.andesite.api.protocol.types.VarInt
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import net.benwoodworth.knbt.buildNbtCompound
import net.benwoodworth.knbt.put

fun main() {
  val codec = MinecraftCodec { protocolVersion = 756 }

  val packet = JoinGamePacket(
    entityId = 0,
    isHardcore = false,
    gameMode = GameMode.Adventure,
    previousGameMode = PreviousGameMode.Unknown,
    worlds = listOf("world"),
    dimensionCodec = buildNbtCompound { put("hello", "world") },
    dimension = buildNbtCompound { put("hello", "world") },
    world = "world",
    hashedSeed = 0,
    maxPlayers = VarInt(20),
    viewDistance = VarInt(32),
    reducedDebugInfo = false,
    enableRespawnScreen = false,
    isDebug = false,
    isFlat = true,
  )

  val bytes = codec.encodeToByteArray(packet)

  println("Packet ${codec.decodeFromByteArray<JoinGamePacket>(bytes)}")
}
