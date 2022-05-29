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

import kotlin.reflect.KClass

public interface Pattern {
  public val text: String
  public val exceptionHandlers: Set<ExceptionHandler>
  public val executionHandlers: Map<KClass<*>, Execution<*>>
}

public interface PatternBuilder {
  public fun onFailure(handler: ExceptionHandler)

  public fun <S : Any> onExecution(type: KClass<S>, handler: Execution<S>)
}

internal class PatternBuilderImpl(val text: String) : PatternBuilder {
  val exceptionHandlers: MutableSet<ExceptionHandler> = mutableSetOf()
  val executionHandlers: MutableMap<KClass<*>, Execution<*>> = mutableMapOf()

  override fun onFailure(handler: ExceptionHandler) {
    exceptionHandlers += handler
  }

  @Suppress("UNCHECKED_CAST")
  override fun <S : Any> onExecution(type: KClass<S>, handler: Execution<S>) {
    executionHandlers[type] = handler as Execution<*>
  }

  fun build(): Pattern {
    return PatternImpl(text, exceptionHandlers, executionHandlers)
  }
}

internal class PatternImpl(
  override val text: String,
  override val exceptionHandlers: Set<ExceptionHandler>,
  override val executionHandlers: Map<KClass<*>, Execution<*>>,
) : Pattern

public inline fun <reified S : Any> PatternBuilder.onExecution(
  noinline handler: suspend ExecutionScope<S>.() -> Unit,
) {
  return onExecution(S::class, handler)
}
