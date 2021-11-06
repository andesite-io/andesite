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

package com.gabrielleeg1.javarock.api.protocol.java.handshake

import com.gabrielleeg1.javarock.api.protocol.Codec
import com.gabrielleeg1.javarock.api.protocol.Packet
import com.gabrielleeg1.javarock.api.protocol.chat.Chat
import com.gabrielleeg1.javarock.api.protocol.java.JavaPacket
import com.gabrielleeg1.javarock.api.protocol.writeString
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.buildPacket
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Response(
  val version: Version,
  val players: Players,
  val description: Chat,
  val favicon: String? = null,
)

@Serializable
data class Version(
  val name: String,
  val protocol: Int,
)

@Serializable
data class Players(
  val max: Int,
  val online: Int,
  val sample: List<Sample> = emptyList(),
)

// TODO: use Uuid type for id field
@Serializable
data class Sample(val name: String, val id: String)

@Packet(0x00)
data class ResponsePacket(val value: Response) : JavaPacket {
  companion object ResponseCodec : Codec<ResponsePacket> {
    val json = Json {}

    override fun write(value: ResponsePacket): ByteReadPacket = buildPacket {
      writeString(json.encodeToString(value))
    }
  }
}
