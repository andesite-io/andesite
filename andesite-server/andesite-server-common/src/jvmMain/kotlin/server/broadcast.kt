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

package andesite.server

import andesite.protocol.misc.Chat
import andesite.protocol.misc.ChatBuilder
import andesite.protocol.misc.mordant

/**
 * Broadcasts the [chat] to all connected clients.
 *
 * @param chat the [Chat] component to broadcast
 */
public suspend fun MinecraftServer.broadcast(chat: Chat) {
  logger.info(chat.mordant())

  players.forEach { player ->
    player.sendMessage(chat)
  }
}

/**
 * Broadcasts the [text] to all connected clients.
 *
 * @param text the base text
 * @param builder the builder function for the full-featured component
 */
public suspend fun MinecraftServer.broadcast(text: String, builder: ChatBuilder.() -> Unit = {}) {
  broadcast(Chat.build(text, builder))
}
