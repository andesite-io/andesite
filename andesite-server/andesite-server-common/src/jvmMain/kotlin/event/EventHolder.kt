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

package andesite.event

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/** Holds an event bus that can be used to listen [E] events. */
public interface EventHolder<E : MinecraftEvent> : CoroutineScope {
  /**
   * The event bus for [E].
   *
   * @return the event bus for [E]
   */
  public fun eventFlow(): Flow<E>
}

/**
 * Listen events on a [EventHolder].
 *
 * @param E the type of the [MinecraftEvent] to listen for.
 * @param context the [CoroutineContext] to use for the coroutine.
 * @param handle the function to execute when an event of type [E] is received.
 */
public inline fun <reified E : MinecraftEvent> EventHolder<out MinecraftEvent>.on(
  context: CoroutineContext = EmptyCoroutineContext,
  noinline handle: suspend E.() -> Unit,
) {
  launch(context + CoroutineName("listen/${E::class.simpleName}")) {
    eventFlow()
      .filterIsInstance<E>()
      .onEach { handle(it) }
      .collect()
  }
}
