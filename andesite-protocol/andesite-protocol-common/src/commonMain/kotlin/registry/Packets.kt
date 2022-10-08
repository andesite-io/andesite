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

package andesite.protocol.registry

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
public data class Packets(
  @SerialName("HANDSHAKING") val handshaking: PacketModeEntry = PacketModeEntry(-1),
  @SerialName("PLAY") val play: PacketModeEntry = PacketModeEntry(0),
  @SerialName("STATUS") val status: PacketModeEntry = PacketModeEntry(1),
  @SerialName("LOGIN") val login: PacketModeEntry = PacketModeEntry(2),
)

@Serializable
public data class PacketModeEntry(
  val id: Int,
  @SerialName("SERVERBOUND") val serverBound: PacketEntryList = PacketEntryList(),
  @SerialName("CLIENTBOUND") val clientBound: PacketEntryList = PacketEntryList(),
)

@Serializable(PacketEntryList.PacketEntryListSerializer::class)
public class PacketEntryList(public val value: List<PacketEntry> = emptyList()) {
  private val map = value.associateBy { it.id }

  public operator fun get(id: Int): PacketEntry? = map[id]

  internal object PacketEntryListSerializer : KSerializer<PacketEntryList> {
    override val descriptor: SerialDescriptor = ListSerializer(PacketEntry.serializer()).descriptor

    override fun serialize(encoder: Encoder, value: PacketEntryList) {
      encoder.encodeSerializableValue(ListSerializer(PacketEntry.serializer()), value.value)
    }

    override fun deserialize(decoder: Decoder): PacketEntryList {
      return PacketEntryList(ListSerializer(PacketEntry.serializer()).deserialize(decoder))
    }
  }
}

@Serializable
public data class PacketEntry(val name: String, val id: Int)
