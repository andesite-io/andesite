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

import andesite.protocol.registry.readPackets
import andesite.protocol.serialization.CodecBuilder
import andesite.protocol.serialization.DefaultProtocolConfiguration
import andesite.protocol.serialization.MinecraftCodec
import andesite.protocol.serialization.MinecraftCodec.Versions
import andesite.protocol.serialization.ProtocolConfiguration

public fun Versions.v756(
  from: ProtocolConfiguration = DefaultProtocolConfiguration,
  builder: CodecBuilder,
): MinecraftCodec {
  return MinecraftCodec(from) {
    protocolVersion = 756
    packetRegistry = createPacketRegistry(readPackets("v756/packets.json")) {
      register<ChunkDataPacket>()
      register<JoinGamePacket>()
      register<KeepAlivePacket>()
      register<PlayerPositionAndLookPacket>()
      register<ServerKeepAlivePacket>()
      register<ServerChatMessagePacket>()
      register<PlayerPositionPacket>()
      register<PlayerRotationPacket>()
      register<PlayerPositionAndRotationPacket>()
    }

    builder()
  }
}
