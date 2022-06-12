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

import andesite.komanda.errors.CommandNotFoundException
import andesite.komanda.errors.NoSwitchablePatternException
import andesite.komanda.errors.NoSwitchableTargetException
import andesite.komanda.errors.ParameterNotFoundException
import andesite.komanda.execution.GroupException
import andesite.komanda.execution.MatchException
import andesite.komanda.execution.Matcher
import andesite.komanda.execution.RawArguments
import andesite.komanda.parsing.ExecutionNode
import andesite.komanda.parsing.parseCommandString
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.withContext

public interface KomandaRoot<S : Any> {
  public val komanda: KomandaSettings

  public fun komanda(configure: KomandaSettingsBuilder.() -> Unit)

  public fun command(name: String, builder: CommandBuilder.() -> Unit)

  public suspend fun dispatch(string: String, sender: S)
}

public abstract class AbstractKomandaRoot<S : Any>(
  builder: KomandaSettingsBuilder.() -> Unit = {},
) : KomandaRoot<S> {
  public val commands: MutableMap<String, Command> = mutableMapOf()

  public override var komanda: KomandaSettings = KomandaSettings(DefaultKomandaSettings, builder)

  public abstract fun createExecutionScope(sender: S, arguments: Arguments): ExecutionScope<S>

  override fun komanda(configure: KomandaSettingsBuilder.() -> Unit) {
    komanda = KomandaSettings(komanda, configure)
  }

  override fun command(name: String, builder: CommandBuilder.() -> Unit) {
    commands[name] = CommandBuilder(name).apply(builder).build()
  }

  override suspend fun dispatch(string: String, sender: S) {
    val executionNodes = parseCommandString(string)
    if (executionNodes.isEmpty()) return

    val name = executionNodes.first()
    val argumentExecutionNodes = executionNodes.drop(1)

    val command = commands[name.text]
      ?: throw CommandNotFoundException(name.text)

    val resultingError = command.children
      .map { pattern ->
        runCatching {
          val arguments: Arguments = pattern.adaptRawArguments(
            rawArguments = parseArguments(argumentExecutionNodes, pattern),
          )

          return command.handlePattern(pattern, sender, arguments)
        }
      }
      .fold(null as Throwable?) { _, next ->
        next.exceptionOrNull()
      }

    val fallbackArguments = Arguments(emptyMap(), executionNodes.joinToString(" ") { it.fullText })

    if (resultingError != null && !command.handleFallback(sender, fallbackArguments)) {
      throw resultingError
    }
  }

  private suspend fun Command.handleFallback(sender: S, arguments: Arguments): Boolean {
    if (fallback == null) return false

    val handler = fallback.executionHandlers[sender::class]
      ?: fallback.executionHandlers[Any::class]
      ?: throw NoSwitchableTargetException(sender::class)

    withContext(CoroutineName("command/$name/fallback")) {
      handler.invoke(createExecutionScope(sender, arguments))
    }

    return true
  }

  private suspend fun Command.handlePattern(pattern: Pattern, sender: S, arguments: Arguments) {
    val handler = pattern.executionHandlers[sender::class]
      ?: pattern.executionHandlers[Any::class]
      ?: throw NoSwitchableTargetException(sender::class)

    withContext(CoroutineName("command/$name/pattern/${pattern.expr}")) {
      val currentScope = createExecutionScope(sender, arguments)
      // propagate scope for the command arguments
      pattern.propagateScope(currentScope)
      handler.invoke(currentScope)
    }
  }

  private suspend fun Pattern.adaptRawArguments(rawArguments: RawArguments): Arguments {
    val arguments = buildMap {
      rawArguments.forEach { (name, executionNode) ->
        val parameter = parameters[name] ?: throw ParameterNotFoundException(name)

        put(name, parameter.executes(executionNode.text) as Any?)
      }
    }

    return Arguments(arguments)
  }

  private fun parseArguments(executionNodes: List<ExecutionNode>, pattern: Pattern): RawArguments {
    try {
      return Matcher
        .group(executionNodes, pattern.expr)
        .map { it.tryAsArguments() }
        .fold(mapOf()) { acc, next ->
          acc + next.getOrThrow()
        }
    } catch (exception: MatchException) {
      throw NoSwitchablePatternException(exception.message)
    } catch (exception: GroupException) {
      throw NoSwitchablePatternException(exception.message)
    }
  }
}
