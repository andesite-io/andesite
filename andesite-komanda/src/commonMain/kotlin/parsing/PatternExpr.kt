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

package andesite.komanda.parsing

import kotlin.reflect.KClass

@JvmInline
public value class PatternExpr(public val nodes: List<PatternNode>) {
  public val size: Int get() = nodes.size

  public constructor(node: PatternNode) : this(listOf(node))

  public fun drop(n: Int): PatternExpr {
    return PatternExpr(nodes.drop(n))
  }

  public fun subExpr(fromIndex: Int, toIndex: Int): PatternExpr {
    return PatternExpr(nodes.subList(fromIndex, toIndex))
  }

  public inline fun forEach(f: (PatternNode) -> Unit) {
    nodes.forEach(f)
  }

  public inline fun forEachIndexed(f: (Int, PatternNode) -> Unit) {
    nodes.forEachIndexed(f)
  }

  override fun toString(): String {
    return nodes.joinToString(" ") { node ->
      buildString {
        when (node) {
          is ArgumentNode<*> -> when {
            node.optional -> append("[${node.name}:${node.type.simpleName}]")
            node.vararg -> append("[...${node.name}:${node.type.simpleName}]")
            else -> append("<${node.name}:${node.type.simpleName}>")
          }
          is PathNode -> when (node.identifiers.size) {
            1 -> append(node.identifiers.first())
            else -> append(node.identifiers.joinToString("|"))
          }
        }
      }
    }
  }
}

public sealed interface PatternNode

public data class ArgumentNode<A : Any>(
  val type: KClass<A>,
  val name: String,
  val optional: Boolean = false,
  val vararg: Boolean = false,
) : PatternNode {
  override fun toString(): String {
    return "ArgumentNode<${type.simpleName}>(name=$name, optional=$optional, vararg=$vararg)"
  }
}

public data class PathNode(val identifiers: Set<String>) : PatternNode {
  public constructor(name: String) : this(setOf(name))
}

public fun parsePatternNode(string: String): PatternExpr {
  TODO()
}
