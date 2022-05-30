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

import andesite.komanda.parsing.ArgumentNode
import andesite.komanda.parsing.ExecutionNode
import andesite.komanda.parsing.IntersectionNode
import andesite.komanda.parsing.OptionalNode
import andesite.komanda.parsing.PathNode
import andesite.komanda.parsing.PatternNode
import andesite.komanda.parsing.SimpleNode
import andesite.komanda.parsing.VarargNode

public fun <S : Any> PatternNode.combine(node: ExecutionNode): Boolean {
  return when (this) {
    is PathNode -> {
      val simple = (node as? SimpleNode) ?: return false
      if (simple.quote) return false

      true
    }
    is IntersectionNode -> {
      val simple = (node as? SimpleNode) ?: return false
      if (simple.quote) return false

      identifiers.contains(simple.text)
    }
    is ArgumentNode<*> -> {
      when (node.name) {
        null -> name == node.name
        else -> true
      }
    }
    is OptionalNode -> TODO()
    is VarargNode -> TODO()
  }
}
