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
import andesite.komanda.parsing.PatternExpr
import kotlin.reflect.KClass

public class FallbackBuilder(private val name: String) {
  private val exceptionHandlers: MutableSet<ExceptionHandler> = mutableSetOf()
  private val executionHandlers: MutableMap<KClass<*>, Execution<*>> = mutableMapOf()

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
    return Pattern(
      expr = PatternExpr(PathNode(name)),
      parameters = emptyMap(),
      exceptionHandlers,
      executionHandlers,
    )
  }
}
