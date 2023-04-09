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

import andesite.protocol.ProtocolEnum
import andesite.protocol.ProtocolNbt
import andesite.protocol.ProtocolPacket
import andesite.protocol.ProtocolValue
import andesite.protocol.ProtocolVariant
import andesite.protocol.Variant
import andesite.protocol.java.JavaPacket
import andesite.protocol.java.data.Dimension
import andesite.protocol.java.data.DimensionCodec
import andesite.protocol.misc.Identifier
import andesite.protocol.types.VarInt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@ProtocolPacket(0x26)
@SerialName("JoinGamePacket")
@Serializable
public data class JoinGamePacket(
  val entityId: Int,
  val isHardcore: Boolean,
  val gameMode: GameMode,
  val previousGameMode: PreviousGameMode,
  val worlds: List<Identifier>,
  @ProtocolNbt
  val dimensionCodec: DimensionCodec,
  @ProtocolNbt
  val dimension: Dimension,
  val world: Identifier,
  val hashedSeed: Long,
  val maxPlayers: VarInt,
  val viewDistance: VarInt,
  val reducedDebugInfo: Boolean,
  val enableRespawnScreen: Boolean,
  val isDebug: Boolean,
  val isFlat: Boolean,
) : JavaPacket

@Serializable
@SerialName("PreviousGameMode")
@ProtocolEnum
@ProtocolVariant(Variant.Byte)
public enum class PreviousGameMode {
  @ProtocolValue(-1)
  Unknown,

  @ProtocolValue(0)
  Survival,

  @ProtocolValue(1)
  Creative,

  @ProtocolValue(2)
  Adventure,

  @ProtocolValue(3)
  Spectator,

  ;

  public fun toGameMode(): GameMode? {
    return when (this) {
      Unknown -> null
      else -> GameMode.values()[ordinal]
    }
  }
}

@Serializable
@SerialName("GameMode")
@ProtocolEnum
@ProtocolVariant(Variant.UByte)
public enum class GameMode {
  @ProtocolValue(0)
  Survival,

  @ProtocolValue(1)
  Creative,

  @ProtocolValue(2)
  Adventure,

  @ProtocolValue(3)
  Spectator,
}
