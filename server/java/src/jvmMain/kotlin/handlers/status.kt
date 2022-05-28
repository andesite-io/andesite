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

package andesite.server.java.handlers

import andesite.protocol.java.handshake.HandshakePacket
import andesite.protocol.java.handshake.PingPacket
import andesite.protocol.java.handshake.Players
import andesite.protocol.java.handshake.PongPacket
import andesite.protocol.java.handshake.Response
import andesite.protocol.java.handshake.ResponsePacket
import andesite.protocol.java.handshake.Version
import andesite.server.java.player.Session
import andesite.server.java.player.receivePacket
import andesite.server.java.player.sendPacket
import andesite.server.java.server.JavaGameServer

internal suspend fun JavaGameServer.handleStatus(session: Session, handshake: HandshakePacket) {
  session.sendPacket(
    ResponsePacket(
      Response(
        version = Version(name = motd.version, protocol = handshake.protocolVersion.toInt()),
        players = Players(max = motd.maxPlayers, online = players.size),
        description = motd.text,
      ),
    ),
  )

  session.receivePacket<PingPacket>()
  session.sendPacket(PongPacket())
}
