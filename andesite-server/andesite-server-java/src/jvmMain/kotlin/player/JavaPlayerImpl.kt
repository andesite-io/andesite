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

package andesite.java.player

import andesite.player.JavaPlayer
import andesite.player.PlayerEvent
import andesite.protocol.java.JavaPacket
import andesite.protocol.java.v756.ChatMessagePacket
import andesite.protocol.java.v756.ChatPosition
import andesite.protocol.misc.Chat
import andesite.protocol.misc.Uuid
import andesite.server.MinecraftServer
import andesite.world.Location
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.serialization.SerializationStrategy

internal class JavaPlayerImpl(
  override val id: Uuid,
  override val protocol: Int,
  override val username: String,
  override var location: Location,
  val session: Session,
  val server: MinecraftServer,
) : JavaPlayer, CoroutineScope by session {
  override fun eventFlow(): Flow<PlayerEvent> {
    return server
      .eventFlow()
      .filterIsInstance<PlayerEvent>()
      .filter { it.player == this }
  }

  override suspend fun sendMessage(chat: Chat) {
    session.sendPacket(ChatMessagePacket(chat, ChatPosition.Chat, uuid4()))
  }

  override suspend fun <A : JavaPacket> sendPacket(
    serializer: SerializationStrategy<A>,
    packet: A,
  ) {
    session.sendPacket(serializer, packet)
  }
}
