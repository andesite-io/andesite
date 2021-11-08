import com.gabrielleeg1.javarock.api.protocol.java.handshake.HandshakePacket
import com.gabrielleeg1.javarock.api.protocol.java.handshake.NextState
import com.gabrielleeg1.javarock.api.protocol.serialization.MinecraftCodec
import com.gabrielleeg1.javarock.api.protocol.types.VarInt
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

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

fun main() {
  val codec = MinecraftCodec { protocolVersion = 756 }

  val bytes = codec.encodeToByteArray(
    HandshakePacket(
      protocolVersion = VarInt.of(756),
      serverAddress = "localhost",
      serverPort = 25565.toUShort(),
      nextState = NextState.Status,
    ),
  )
  
  val packet = codec.decodeFromByteArray<HandshakePacket>(bytes)
  
  println("Packet $packet")
}
