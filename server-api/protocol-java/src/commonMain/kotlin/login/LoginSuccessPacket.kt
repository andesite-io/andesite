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

import com.benasher44.uuid.Uuid
import com.gabrielleeg1.javarock.api.protocol.Codec
import com.gabrielleeg1.javarock.api.protocol.Packet
import com.gabrielleeg1.javarock.api.protocol.java.JavaPacket
import com.gabrielleeg1.javarock.api.protocol.writeString
import com.gabrielleeg1.javarock.api.protocol.writeUuid
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.buildPacket

@Packet(0x02, LoginSuccessPacket.LoginSuccessCodec::class)
data class LoginSuccessPacket(val id: Uuid, val username: String) : JavaPacket {
  companion object LoginSuccessCodec : Codec<LoginSuccessPacket> {
    override fun write(value: LoginSuccessPacket): ByteReadPacket = buildPacket {
      writeUuid(value.id)
      writeString(value.username)
    }
  }
}
