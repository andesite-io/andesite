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

import com.gabrielleeg1.javarock.api.protocol.Packet
import com.gabrielleeg1.javarock.api.protocol.ProtocolEnum
import com.gabrielleeg1.javarock.api.protocol.java.JavaPacket
import com.gabrielleeg1.javarock.api.protocol.types.VarInt
import kotlinx.serialization.Serializable

@Packet(0x00)
@Serializable
data class HandshakePacket(
  val protocolVersion: VarInt,
  val serverAddress: String,
  val serverPort: UShort,
  val nextState: NextState,
) : JavaPacket

@ProtocolEnum
@Serializable
enum class NextState {
  @ProtocolEnum.Entry(1)
  Status,

  @ProtocolEnum.Entry(2)
  Login;
}
