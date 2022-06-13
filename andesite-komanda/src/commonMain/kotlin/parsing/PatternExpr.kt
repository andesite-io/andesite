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

import andesite.komanda.errors.PatternNodeParsingException
import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separatedTerms
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.h0tk3y.betterParse.parser.Parser
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
          is ParameterNode<*> -> when {
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

public data class ParameterNode<A : Any>(
  val type: KClass<A>,
  val name: String,
  val optional: Boolean = false,
  val vararg: Boolean = false,
) : PatternNode {
  override fun toString(): String {
    return "ParameterNode<${type.simpleName}>(name=$name, optional=$optional, vararg=$vararg)"
  }
}

public data class PathNode(val identifiers: Set<String>) : PatternNode {
  public constructor(name: String) : this(setOf(name))
}

private val QuotedStringRegex: Regex = Regex("""(["'])(?:\\\1|.)*?\1""")
private val IdRegex: Regex = Regex("""\w+""")
private val WhitespaceRegex: Regex = Regex("""\s+""")

public fun parsePatternNode(input: String): PatternExpr {
  val grammar = object : Grammar<List<PatternNode>>() {
    val id by regexToken(IdRegex)
    val quote by regexToken(QuotedStringRegex)
    val ws by regexToken(WhitespaceRegex, ignore = true)
    val gt by literalToken(">")
    val lt by literalToken("<")
    val lBracket by literalToken("[")
    val rBracket by literalToken("]")

    val varargNode: Parser<PatternNode> by skip(lBracket)
      .and(skip(literalToken("...")))
      .and(id)
      .and(skip(rBracket))
      .map { match ->
        ParameterNode(Any::class, match.text, vararg = true)
      }

    val optionalNode: Parser<PatternNode> by skip(lBracket)
      .and(id)
      .and(skip(rBracket))
      .map { match ->
        ParameterNode(Any::class, match.text, optional = true)
      }

    val parameterNode: Parser<PatternNode> by skip(lt)
      .and(id)
      .and(skip(gt))
      .map { match ->
        ParameterNode(Any::class, match.text)
      }

    val pathNode: Parser<PatternNode> by separatedTerms(
      term = id,
      separator = literalToken("|"),
      acceptZero = false,
    ).map { matches ->
      PathNode(matches.map { it.text }.toSet())
    }

    val node: Parser<PatternNode> by pathNode or parameterNode or optionalNode

    override val rootParser: Parser<List<PatternNode>> by separatedTerms(
      term = node,
      separator = ws,
      acceptZero = true,
    )
  }

  val nodes = try {
    grammar.parseToEnd(input)
  } catch (cause: ParseException) {
    throw PatternNodeParsingException("Pattern node parsing failed", cause)
  }

  if (nodes.dropLast(1).filterIsInstance<ParameterNode<*>>().any { it.vararg }) {
    throw PatternNodeParsingException("Vararg must be the last parameter in a pattern")
  }

  return PatternExpr(nodes)
}
