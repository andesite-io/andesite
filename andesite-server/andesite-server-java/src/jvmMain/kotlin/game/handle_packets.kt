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

package andesite.java.game

import andesite.java.player.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException

internal suspend fun handlePackets(scope: CoroutineScope, session: Session) {
  while (true) {
    try {
      val packet = session.acceptPacket() ?: continue

      session.inboundPacketFlow.emit(packet)
    } catch (_: ClosedReceiveChannelException) {
      break
    } catch (_: Throwable) {
      // nothing
    }
  }
}
