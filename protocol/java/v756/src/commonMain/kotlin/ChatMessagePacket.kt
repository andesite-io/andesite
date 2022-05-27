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
import andesite.protocol.ProtocolJson
import andesite.protocol.ProtocolPacket
import andesite.protocol.ProtocolValue
import andesite.protocol.ProtocolVariant
import andesite.protocol.Variant
import andesite.protocol.java.JavaPacket
import andesite.protocol.misc.Chat
import com.benasher44.uuid.Uuid
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ChatMessagePacket")
@ProtocolPacket(0x0F)
public data class ChatMessagePacket(
  @ProtocolJson
  val data: Chat,
  val position: ChatPosition,
  @Contextual
  val sender: Uuid,
) : JavaPacket

@Serializable
@ProtocolEnum
@SerialName("ChatPosition")
@ProtocolVariant(Variant.Byte)
public enum class ChatPosition {
  @ProtocolValue(0)
  Chat,

  @ProtocolValue(1)
  SystemMessage,

  @ProtocolValue(2)
  ActionBar
}
