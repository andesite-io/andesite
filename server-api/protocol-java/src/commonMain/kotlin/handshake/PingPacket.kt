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
import com.gabrielleeg1.javarock.api.protocol.currentTimeMillis
import com.gabrielleeg1.javarock.api.protocol.java.JavaPacket
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeLong

@Packet(0x01, PingPacket.PingCodec::class)
data class PingPacket(val payload: Long) : JavaPacket {
  companion object PingCodec : Codec<PingPacket> {
    override fun write(value: PingPacket): ByteReadPacket = buildPacket {
      writeLong(value.payload)
    }

    override fun read(packet: ByteReadPacket): PingPacket {
      return PingPacket(currentTimeMillis())
    }
  }
}
