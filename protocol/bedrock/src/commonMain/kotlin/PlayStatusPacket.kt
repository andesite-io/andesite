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

package andesite.protocol.bedrock

import andesite.protocol.ProtocolEnum
import andesite.protocol.ProtocolPacket
import andesite.protocol.ProtocolValue
import andesite.protocol.ProtocolVariant
import andesite.protocol.Variant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PlayStatusPacket")
@ProtocolPacket(0x02)
data class PlayStatusPacket(val status: PlayStatus) : BedrockPacket

@Serializable
@SerialName("PlayStatus")
@ProtocolEnum
@ProtocolVariant(Variant.Int)
enum class PlayStatus {
  @ProtocolValue(0)
  LoginSuccess,

  @ProtocolValue(1)
  FailedClient,

  @ProtocolValue(2)
  FailedServer,

  @ProtocolValue(3)
  PlayerSpawn,

  @ProtocolValue(4)
  FailedInvalidTenant,

  @ProtocolValue(5)
  FailedVanillaEdu,

  @ProtocolValue(6)
  FailedEduVanilla,

  @ProtocolValue(7)
  FailedServerFull;
}
