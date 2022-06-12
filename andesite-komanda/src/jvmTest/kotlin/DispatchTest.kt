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

import andesite.protocol.misc.Chat
import andesite.protocol.misc.mordant
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class DispatchTest {
  @Test
  fun `test dispatch`(): Unit = runBlocking {
    val root = TestKomandaRoot()

    root.command("hello") {
      pattern {
        expr {
          path("world")
          argument<String>("target")
          path("another")
          argument<String>("batata")
        }

        val target by arguments
          .creating<String>()
          .executes { it }

        val batata by arguments
          .creating<String>()
          .executes { it }

        onExecution<String> {
          sendMessage("Hello, target=$target, batata=$batata")
        }
      }

      rootPattern {
        onExecution<String> {
          sendMessage("Hello, $arguments!")
        }
      }
    }

    root.dispatch("hello world 'carlos' another 'batata'", "Gabi")
//    root.dispatch("hello 'world'", "Gabi")
//    root.dispatch("hello target:'world'", "Gabi")
//    root.dispatch("hello target='world'", "Gabi")
//    root.dispatch("hello target= 'world'", "Gabi")
  }
}

class TestExecutionScope<S : Any>(override val sender: S, override val arguments: Arguments) :
  ExecutionScope<S> {
  override suspend fun sendMessage(chat: Chat) {
    println("message: ${chat.mordant()}")
  }

  override suspend fun failwith(chat: Chat) {
    println("fail: ${chat.mordant()}")
  }
}

class TestKomandaRoot : AbstractKomandaRoot<String>() {
  override fun createExecutionScope(sender: String, arguments: Arguments) =
    TestExecutionScope(sender, arguments)
}
