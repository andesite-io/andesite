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

package andesite.command

import andesite.komanda.AbstractKomandaRoot
import andesite.komanda.Arguments
import andesite.komanda.ExecutionScope
import andesite.player.MinecraftPlayer
import andesite.protocol.misc.Chat
import andesite.server.Messageable

public class MinecraftKomandaRoot : AbstractKomandaRoot<Messageable>(
  {
    alias<Int>("int")
    alias<Double>("double")
    alias<Float>("float")
    alias<Byte>("byte")
    alias<Short>("short")

    onFailure { failure ->
      when (failure.cause) {
        null -> sendMessage(failure.chat)
        else -> sendMessage(
          "&cAn internal error occurred," +
            " please contact the server administration",
        )
      }
    }

    acceptTargets {
      put(MinecraftPlayer::class, Chat.of("&cThe executor must be a player"))
    }
  },
) {
  override fun createExecutionScope(
    sender: Messageable,
    arguments: Arguments,
  ): ExecutionScope<Messageable> {
    return MinecraftExecutionScope(arguments, sender)
  }
}
