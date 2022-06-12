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

package andesite.komanda

import andesite.komanda.parsing.parsePatternNode
import andesite.protocol.misc.Chat
import andesite.protocol.misc.ChatListBuilder

public data class Command(
  val fallback: Pattern?,
  val name: String,
  val usage: List<Chat>,
  val aliases: Set<String>,
  val permissions: Set<String>,
  val children: Set<Pattern>,
)

public class CommandBuilder(private val name: String) {
  public var permissions: List<String> = listOf()
  public var aliases: List<String> = listOf()
  public var usage: List<Chat> = listOf()

  private var fallback: Pattern? = null
  private val children: MutableSet<Pattern> = mutableSetOf()

  public fun fallback(builder: FallbackBuilder.() -> Unit) {
    fallback = FallbackBuilder(name).apply(builder).build()
  }

  public fun usage(builder: ChatListBuilder.() -> Unit) {
    usage = Chat.many(builder)
  }

  public fun pattern(text: String, builder: PatternBuilder.() -> Unit) {
    children += PatternBuilder(parsePatternNode(text)).apply(builder).build()
  }

  public fun pattern(builder: PatternBuilder.() -> Unit) {
    children += PatternBuilder().apply(builder).build()
  }

  public fun build(): Command {
    return Command(fallback, name, usage, aliases.toSet(), permissions.toSet(), children)
  }
}
