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

import andesite.komanda.parsing.PathNode
import andesite.komanda.parsing.parsePatternNode
import andesite.protocol.misc.Chat
import andesite.protocol.misc.ChatListBuilder
import kotlin.reflect.KClass

public data class Command(
  val rootPattern: Pattern,
  val name: String,
  val usage: List<Chat>,
  val aliases: Set<String>,
  val permissions: Set<String>,
  val children: Set<Pattern>,
)

public class CommandBuilder(private val name: String) : HasExecutor {
  public var permissions: List<String> = listOf()
  public var aliases: List<String> = listOf()
  public var usage: List<Chat> = listOf()

  private val children: MutableSet<Pattern> = mutableSetOf()

  private val exceptionHandlers: MutableSet<ExceptionHandler> = mutableSetOf()
  private val executionHandlers: MutableMap<KClass<*>, Execution<*>> = mutableMapOf()

  public fun usage(builder: ChatListBuilder.() -> Unit) {
    usage = Chat.many(builder)
  }

  public fun pattern(text: String, builder: PatternBuilder.() -> Unit) {
    children += PatternBuilder(parsePatternNode(text)).apply(builder).build()
  }

  public fun pattern(builder: PatternBuilder.() -> Unit) {
    children += PatternBuilder().apply(builder).build()
  }

  public fun onFailure(handler: ExceptionHandler) {
    exceptionHandlers += handler
  }

  override fun <S : Any> onExecution(type: KClass<S>, handler: Execution<S>) {
    @Suppress("UNCHECKED_CAST")
    executionHandlers[type] = handler as Execution<*>
  }

  public fun build(): Command {
    val nodes = listOf(PathNode(name))
    val rootPattern = Pattern(nodes, exceptionHandlers, executionHandlers)

    return Command(rootPattern, name, usage, aliases.toSet(), permissions.toSet(), children)
  }
}
