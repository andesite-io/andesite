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

public interface EventHolder<E : MinecraftEvent> : CoroutineScope {
  public fun eventFlow(): Flow<E>
}

public inline fun <reified E : MinecraftEvent> EventHolder<out MinecraftEvent>.on(
  crossinline handle: suspend E.() -> Unit,
) {
  launch(CoroutineName("listen-${E::class.simpleName}")) {
    eventFlow()
      .filterIsInstance<E>()
      .onEach { handle(it) }
      .collect()
  }
}
