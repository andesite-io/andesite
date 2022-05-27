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

package andesite.server.java.game

import andesite.player.JavaPlayer
import andesite.protocol.java.v756.ServerChatMessagePacket
import andesite.server.java.player.Session
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal suspend fun handleChat(session: Session, player: JavaPlayer): Unit = coroutineScope {
  launch(Job()) {
    session.inboundPacketChannel
      .consumeAsFlow()
      .filterIsInstance<ServerChatMessagePacket>()
      .onEach { packet -> player.sendMessage(packet.message) }
      .collect()
  }
}