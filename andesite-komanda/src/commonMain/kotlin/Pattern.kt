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
import andesite.komanda.parsing.PathNode
import andesite.komanda.parsing.PatternExpr
import andesite.komanda.parsing.PatternNode
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

public data class Pattern(
  val expr: PatternExpr,
  val parameters: Map<String, Parameter<*>>,
  val exceptionHandlers: Set<ExceptionHandler>,
  val executionHandlers: Map<KClass<*>, Execution<*>>,
) {
  public suspend fun propagateScope(executionScope: ExecutionScope<*>) {
    return suspendCoroutine { cont ->
      parameters.values.forEach { argument ->
        argument.localScope = executionScope
      }

      cont.resume(Unit)
    }
  }
}

public class PatternBuilder(private var node: PatternExpr? = null) {
  private val exceptionHandlers: MutableSet<ExceptionHandler> = mutableSetOf()
  private val executionHandlers: MutableMap<KClass<*>, Execution<*>> = mutableMapOf()

  public val arguments: ParametersBuilder = ParametersBuilder()

  public fun expr(builder: PatternExprBuilder.() -> Unit) {
    node = PatternExprBuilder().apply(builder).build()
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
    val node = requireNotNull(node) { "The node must be set to build a Pattern" }
    val arguments = arguments.build().associateBy { it.name }

    return Pattern(node, arguments, exceptionHandlers, executionHandlers)
  }
}

public class PatternExprBuilder {
  private val nodes: MutableList<PatternNode> = mutableListOf()

  public fun path(vararg names: String): PathNode {
    return PathNode(names.toSet()).also(nodes::add)
  }

  public fun <A : Any> vararg(type: KClass<A>, name: String): ArgumentNode<A> {
    return ArgumentNode(type, name, vararg = true).also(nodes::add)
  }

  public inline fun <reified A : Any> vararg(name: String): ArgumentNode<A> {
    return vararg(A::class, name)
  }

  public fun <A : Any> optional(type: KClass<A>, name: String): ArgumentNode<A> {
    return ArgumentNode(type, name, optional = true).also(nodes::add)
  }

  public inline fun <reified A : Any> optional(name: String): ArgumentNode<A> {
    return optional(A::class, name)
  }

  public fun <A : Any> argument(type: KClass<A>, name: String): ArgumentNode<A> {
    return ArgumentNode(type, name).also(nodes::add)
  }

  public inline fun <reified A : Any> argument(name: String): ArgumentNode<A> {
    return argument(A::class, name)
  }

  public fun build(): PatternExpr {
    return PatternExpr(nodes)
  }
}
