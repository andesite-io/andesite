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

package andesite.komanda.execution

import andesite.komanda.Arguments
import andesite.komanda.parsing.ArgumentNode
import andesite.komanda.parsing.ExecutionNode
import andesite.komanda.parsing.NamedNode
import andesite.komanda.parsing.PatternExpr

internal data class NodeGroup(val executionNodes: List<ExecutionNode>, val expr: PatternExpr) {
  internal fun tryAsArguments(): Result<Arguments> {
    return try {
      Result.success(asArguments())
    } catch (failure: MatchException) {
      Result.failure(failure)
    }
  }

  internal fun asArguments(): Arguments {
    val executionNodes = executionNodes.drop(1)
    val expr = expr.drop(1)

    val arguments = buildMap {
      // if the node group has a named argument, all other arguments should be named too,
      // so this will handle named node group.
      if (executionNodes.filterIsInstance<NamedNode>().isNotEmpty()) {
        expr.forEach { node ->
          when (node) {
            is ArgumentNode<*> -> {
              val argumentNode = executionNodes.find { it.name == node.name }
                ?: throw MatchException("Could not find argument with name ${node.name}")

              put(node.name, argumentNode)

              Unit
            }
            else -> {}
          }
        }
      } else {
        executionNodes.forEachIndexed { index, executionNode ->
          val patternNode = expr.nodes.elementAtOrNull(index)
            ?: error("Could not find pattern node with at index $index")

          if (patternNode !is ArgumentNode<*>) {
            throw MatchException("Pattern node found at $index is not a an argument")
          }

          put(patternNode.name, executionNode) // TODO: map into a real world value
        }
      }
    }

    return Arguments(arguments)
  }
}

internal class MatchException(override val message: String) : RuntimeException()
