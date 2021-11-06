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
import com.gabrielleeg1.javarock.api.protocol.VarIntEnum
import com.gabrielleeg1.javarock.api.protocol.java.JavaPacket
import com.gabrielleeg1.javarock.api.protocol.readString
import com.gabrielleeg1.javarock.api.protocol.readVarInt
import com.gabrielleeg1.javarock.api.protocol.types.VarInt
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.readUShort

@Packet(0x00, HandshakePacket.HandshakeCodec::class)
data class HandshakePacket(
  val protocolVersion: VarInt,
  val serverAddress: String,
  val serverPort: UShort,
  val nextState: NextState,
) : JavaPacket {

  companion object HandshakeCodec : Codec<HandshakePacket> {
    @ExperimentalUnsignedTypes
    override fun read(packet: ByteReadPacket): HandshakePacket {
      val protocolVersion = packet.readVarInt()
      val serverAddress = packet.readString(255)
      val serverPort = packet.readUShort()
      val nextState = when (val nextState = packet.readVarInt().toInt()) {
        1 -> NextState.Status
        2 -> NextState.Login
        else -> error("Unknown next state: $nextState")
      }

      return HandshakePacket(protocolVersion, serverAddress, serverPort, nextState)
    }
  }
}

@VarIntEnum
enum class NextState {
  @VarIntEnum.Entry(1)
  Status,
  
  @VarIntEnum.Entry(2)
  Login;
}
