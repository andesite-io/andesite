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

public data class Pattern(
  val node: List<PatternNode>,
  val exceptionHandlers: Set<ExceptionHandler>,
  val executionHandlers: Map<KClass<*>, Execution<*>>,
)

public class PatternBuilder(private var node: List<PatternNode>? = null) {
  private val exceptionHandlers: MutableSet<ExceptionHandler> = mutableSetOf()
  private val executionHandlers: MutableMap<KClass<*>, Execution<*>> = mutableMapOf()

  public val arguments: ArgumentListBuilder get() = TODO()

  public fun node(builder: PatternNodeListBuilder.() -> Unit) {
    node = PatternNodeListBuilder().apply(builder).build()
  }

  public fun onFailure(handler: ExceptionHandler) {
    exceptionHandlers += handler
  }

  public inline fun <reified S : Any> onExecution(noinline handler: Execution<S>) {
    onExecution(S::class, handler)
  }

  public fun onAnyExecution(handler: Execution<Any>) {
    onExecution(Any::class, handler)
  }

  public fun <S : Any> onExecution(type: KClass<S>, handler: Execution<S>) {
    @Suppress("UNCHECKED_CAST")
    executionHandlers[type] = handler as Execution<*>
  }

  public fun build(): Pattern {
    requireNotNull(node) { "The node must be set to build a Pattern" }

    return Pattern(node!!, exceptionHandlers, executionHandlers)
  }
}

public class PatternNodeListBuilder {
  private val nodes: MutableList<PatternNode> = mutableListOf()

  public fun <A : Any> vararg(type: KClass<A>, name: String): VarargNode<A> {
    return VarargNode(type, name).also(nodes::add)
  }

  public inline fun <reified A : Any> vararg(name: String): VarargNode<A> {
    return vararg(A::class, name)
  }

  public fun <A : Any> optional(type: KClass<A>, name: String): OptionalNode<A> {
    return OptionalNode(type, name).also(nodes::add)
  }

  public inline fun <reified A : Any> optional(name: String): OptionalNode<A> {
    return optional(A::class, name)
  }

  public fun <A : Any> argument(type: KClass<A>, name: String): ArgumentNode<A> {
    return ArgumentNode(type, name).also(nodes::add)
  }

  public inline fun <reified A : Any> argument(name: String): ArgumentNode<A> {
    return argument(A::class, name)
  }

  public fun path(name: String): PathNode {
    return PathNode(name).also(nodes::add)
  }

  public fun intersection(vararg identifiers: String): IntersectionNode {
    return IntersectionNode(identifiers.toSet()).also(nodes::add)
  }

  public fun build(): List<PatternNode> {
    return nodes
  }
}
