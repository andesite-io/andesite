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

@file:OptIn(ExperimentalTime::class)

package andesite.server.java.handlers

import andesite.player.JavaPlayer
import andesite.player.PlayerChatEvent
import andesite.protocol.java.v756.ServerChatMessagePacket
import andesite.protocol.misc.Chat
import andesite.server.java.player.Session
import andesite.server.java.server.JavaMinecraftServer
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import org.apache.logging.log4j.kotlin.logger

private val logger = logger("andesite.handlers.Chat")

internal suspend fun JavaMinecraftServer.handleChat(session: Session, player: JavaPlayer) {
  session.inboundPacketFlow
    .filterIsInstance<ServerChatMessagePacket>()
    .onEach { packet ->
      publish(PlayerChatEvent(Chat.of(packet.message), player))
    }
    .collect()
}
