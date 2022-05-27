/*
 *    Copyright 2021 Gabrielle Guimarães de Oliveira
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

package andesite.server.java

import java.lang.System.getSecurityManager
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext

suspend fun main(): Unit = withContext(scope.coroutineContext) {
  startAndesite()
}

private val context = Executors
  .newCachedThreadPool(AndesiteThreadFactory)
  .asCoroutineDispatcher()

private val scope = CoroutineScope(context)

private object AndesiteThreadFactory : ThreadFactory {
  const val NAME_PREFIX = "andesite-pool-"
  val threadNumber = AtomicInteger(0)
  val group: ThreadGroup = getSecurityManager()?.threadGroup ?: Thread.currentThread().threadGroup

  override fun newThread(runnable: Runnable): Thread {
    return Thread(group, runnable, NAME_PREFIX + threadNumber.incrementAndGet(), 0).apply {
      isDaemon = false
      priority = Thread.NORM_PRIORITY
    }
  }
}
