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

import andesite.komanda.parsing.ExecutionNode
import andesite.komanda.parsing.PathNode
import andesite.komanda.parsing.PatternExpr
import andesite.komanda.parsing.SimpleNode

internal object Matcher {
  fun group(executionNodes: List<ExecutionNode>, rootExpr: PatternExpr): List<NodeGroup> =
    buildList {
      var recording: Int? = null

      rootExpr.forEachIndexed { i, patternNode ->
        val node = executionNodes.elementAtOrNull(i)
          ?: throw GroupException("Could not find node $i")

        when (patternNode) {
          is PathNode -> {
            val simpleNode = node as? SimpleNode
              ?: throw GroupException("Path should be a simple node")
            if (simpleNode.quote) throw GroupException("Path should be not be quoted")

            if (recording != null) {
              val currentExecutionNodes = executionNodes.subList(recording!!, i)
              val currentPatternExpr = rootExpr.subExpr(recording!!, i)

              add(NodeGroup(currentExecutionNodes, currentPatternExpr))
            }

            recording = i
          }
          else -> {}
        }
      }

      if (recording != null) {
        val currentExecutionNodes = executionNodes.subList(recording!!, executionNodes.size)
        val currentPatternExpr = rootExpr.subExpr(recording!!, rootExpr.size)

        add(NodeGroup(currentExecutionNodes, currentPatternExpr))
      }
    }
}

internal typealias RawArguments = Map<String, ExecutionNode>

internal class GroupException(override val message: String) : RuntimeException()
