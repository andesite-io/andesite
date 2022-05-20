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

package andesite.server.java.player

import andesite.player.JavaPlayer
import andesite.protocol.java.JavaPacket
import com.benasher44.uuid.Uuid

internal class JavaPlayerImpl(
  override val id: Uuid,
  override val protocol: Int,
  override val username: String,
  val session: Session,
) : JavaPlayer {
  override suspend fun sendPacket(packet: JavaPacket, queue: Boolean) {
    session.sendPacket(JavaPacket.serializer(), packet)
  }
}
