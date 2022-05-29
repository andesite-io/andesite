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

import andesite.komanda.parsing.ArgumentNode
import andesite.komanda.parsing.IntersectionNode
import andesite.komanda.parsing.OptionalNode
import andesite.komanda.parsing.PathNode
import andesite.komanda.parsing.PatternNode
import andesite.komanda.parsing.VarargNode
import kotlin.reflect.KClass

public interface Pattern {
  public val node: List<PatternNode>
  public val exceptionHandlers: Set<ExceptionHandler>
  public val executionHandlers: Map<KClass<*>, Execution<*>>
}

public interface PatternBuilder {
  public fun node(builder: PatternNodeListBuilder.() -> Unit)

  public fun onFailure(handler: ExceptionHandler)

  public fun <S : Any> onExecution(type: KClass<S>, handler: Execution<S>)

  public fun onAnyExecution(handler: Execution<Any>) {
    onExecution(Any::class, handler)
  }
}

public inline fun <reified S : Any> PatternBuilder.onExecution(
  noinline handler: suspend ExecutionScope<S>.() -> Unit,
) {
  return onExecution(S::class, handler)
}

public class PatternNodeListBuilder {
  private val nodes: MutableList<PatternNode> = mutableListOf()

  public fun <A : Any> argument(type: KClass<A>, name: String): ArgumentNode<A> {
    return ArgumentNode(type, name).also(nodes::add)
  }

  public fun vararg(name: String): VarargNode {
    return VarargNode(name).also(nodes::add)
  }

  public fun path(name: String): PathNode {
    return PathNode(name).also(nodes::add)
  }

  public fun optional(name: String): OptionalNode {
    return OptionalNode(name).also(nodes::add)
  }

  public fun intersection(vararg identifiers: String): IntersectionNode {
    return IntersectionNode(identifiers.toSet()).also(nodes::add)
  }

  public inline fun <reified A : Any> argument(name: String): ArgumentNode<A> {
    return argument(A::class, name)
  }

  public fun build(): List<PatternNode> {
    return nodes
  }
}

internal class PatternBuilderImpl(var node: List<PatternNode>? = null) : PatternBuilder {
  val exceptionHandlers: MutableSet<ExceptionHandler> = mutableSetOf()
  val executionHandlers: MutableMap<KClass<*>, Execution<*>> = mutableMapOf()

  override fun node(builder: PatternNodeListBuilder.() -> Unit) {
    node = PatternNodeListBuilder().apply(builder).build()
  }

  override fun onFailure(handler: ExceptionHandler) {
    exceptionHandlers += handler
  }

  @Suppress("UNCHECKED_CAST")
  override fun <S : Any> onExecution(type: KClass<S>, handler: Execution<S>) {
    executionHandlers[type] = handler as Execution<*>
  }

  fun build(): Pattern {
    requireNotNull(node) { "The node must be set to build a Pattern" }

    return PatternImpl(node!!, exceptionHandlers, executionHandlers)
  }
}

internal class PatternImpl(
  override val node: List<PatternNode>,
  override val exceptionHandlers: Set<ExceptionHandler>,
  override val executionHandlers: Map<KClass<*>, Execution<*>>,
) : Pattern
