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

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separatedTerms
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

public fun parseCommandString(string: String): List<ExecutionNode> {
  val grammar = object : Grammar<List<ExecutionNode>>() {
    val id by regexToken("""\w+""")
    val quote by regexToken("""([\"'])(?:\\\1|.)*?\1""")
    val ws by regexToken("""\s+""", ignore = true)

    val colon by literalToken(":")
    val equal by literalToken("=")

    val assign by colon or equal

    val simpleArgument: Parser<ExecutionNode> by id
      .and(optional(-assign and parser { argument }))
      .map { (token, node) ->
        when (node) {
          null -> SimpleNode(token.text)
          else -> NamedNode(token.text, node)
        }
      }

    val quotedArgument: Parser<ExecutionNode> by quote map {
      SimpleNode(it.text.substring(1, it.text.length - 1))
    }

    val argument: Parser<ExecutionNode> by parser { simpleArgument } or quotedArgument

    override val rootParser: Parser<List<ExecutionNode>> by
    separatedTerms(argument, ws, acceptZero = true)
  }

  return grammar.parseToEnd(string)
}
