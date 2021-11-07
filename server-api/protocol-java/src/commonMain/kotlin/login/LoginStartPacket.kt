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

package com.gabrielleeg1.javarock.api.protocol.java.login

import com.gabrielleeg1.javarock.api.protocol.Codec
import com.gabrielleeg1.javarock.api.protocol.Packet
import com.gabrielleeg1.javarock.api.protocol.java.JavaPacket
import com.gabrielleeg1.javarock.api.protocol.readString
import io.ktor.utils.io.core.ByteReadPacket

@Packet(0x00, LoginStartPacket.LoginStartCodec::class)
data class LoginStartPacket(val username: String) : JavaPacket {
  companion object LoginStartCodec : Codec<LoginStartPacket> {
    override fun read(packet: ByteReadPacket): LoginStartPacket {
      return LoginStartPacket(packet.readString(16))
    }
  }
}
