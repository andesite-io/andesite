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

public interface Command : Pattern {
  public val name: String
  public val usage: List<Chat>
  public val aliases: Set<String>
  public val permissions: Set<String>
  public val children: Set<Pattern> // TODO: add possibility to pattern own children too
}

public interface CommandBuilder : PatternBuilder {
  public var permissions: List<String>
  public var aliases: List<String>
  public var usage: List<Chat>

  public fun usage(builder: ChatListBuilder.() -> Unit)

  public fun pattern(text: String, builder: PatternBuilder.() -> Unit)

  public fun pattern(builder: PatternBuilder.() -> Unit)
}

internal class CommandBuilderImpl(val name: String) : CommandBuilder {
  override var permissions: List<String> = listOf()
  override var aliases: List<String> = listOf()
  override var usage: List<Chat> = listOf()

  val children: MutableSet<Pattern> = mutableSetOf()

  val exceptionHandlers: MutableSet<ExceptionHandler> = mutableSetOf()
  val executionHandlers: MutableMap<KClass<*>, Execution<*>> = mutableMapOf()

  override fun node(builder: PatternNodeListBuilder.() -> Unit) {
    // nothing to do in root of command
  }

  override fun usage(builder: ChatListBuilder.() -> Unit) {
    usage = Chat.many(builder)
  }

  override fun pattern(text: String, builder: PatternBuilder.() -> Unit) {
    children += PatternBuilderImpl(parsePatternNode(text)).apply(builder).build()
  }

  override fun pattern(builder: PatternBuilder.() -> Unit) {
    children += PatternBuilderImpl().apply(builder).build()
  }

  override fun onFailure(handler: ExceptionHandler) {
    exceptionHandlers += handler
  }

  @Suppress("UNCHECKED_CAST")
  override fun <S : Any> onExecution(type: KClass<S>, handler: Execution<S>) {
    executionHandlers[type] = handler as Execution<*>
  }

  fun build(): Command {
    val nodes = listOf(PathNode(name))
    val rootPattern = PatternImpl(nodes, exceptionHandlers, executionHandlers)

    return CommandImpl(rootPattern, name, usage, aliases.toSet(), permissions.toSet(), children)
  }
}

internal class CommandImpl(
  val rootPattern: Pattern,
  override val name: String,
  override val usage: List<Chat>,
  override val aliases: Set<String>,
  override val permissions: Set<String>,
  override val children: Set<Pattern>,
) : Command, Pattern by rootPattern
