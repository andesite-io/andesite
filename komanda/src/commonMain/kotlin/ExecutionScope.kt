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

import andesite.komanda.parsing.ExecutionNode
import andesite.protocol.misc.Chat
import andesite.protocol.misc.ChatBuilder
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

public interface ExecutionScope<S : Any> {
  public val arguments: Arguments
  public val sender: S

  public suspend fun sendMessage(chat: Chat)

  public suspend fun sendMessage(text: String, builder: ChatBuilder.() -> Unit = {}) {
    sendMessage(Chat.build(text, builder))
  }

  public suspend fun failwith(chat: Chat)

  public suspend fun failwith(text: String, builder: ChatBuilder.() -> Unit = {}) {
    failwith(Chat.build(text, builder))
  }
}

public class Arguments(private val nodes: List<ExecutionNode>) {
  public val size: Int get() = nodes.size

  public fun <A : Any> get(name: String, type: KClass<A>): A {
    TODO()
  }

  public fun <A : Any> getOrNull(name: String, type: KClass<A>): A? {
    TODO()
  }

  public inline fun <reified A : Any> Arguments.orDefault(value: A): ReadOnlyProperty<Any?, A> {
    return ReadOnlyProperty { _, property ->
      getOrNull(property.name, A::class) ?: value
    }
  }

  public inline operator fun <reified A : Any> getValue(thisRef: Any?, property: KProperty<*>): A {
    return get(property.name, A::class)
  }

  public fun toStringList(): List<String> {
    return nodes.map { it.fullText }
  }

  public fun toList(): List<ExecutionNode> {
    return nodes
  }

  override fun toString(): String {
    return toStringList().joinToString(" ")
  }
}
