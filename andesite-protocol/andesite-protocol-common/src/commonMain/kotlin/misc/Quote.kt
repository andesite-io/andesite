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

package andesite.protocol.misc

internal data class Quote(val text: String, val placeholder: Boolean, val fullText: String = text) {
  override fun toString(): String =
    "Quote(" +
      "text=\"$text\"," +
      " placeholder=$placeholder," +
      " fullText=\"$fullText\")"
}

internal fun quoteString(string: String): List<Quote> =
  """((?:\\[{}]|\{}|[^{}])+)|\{([a-zA-Z_][a-zA-Z0-9_]*)+}"""
    .toRegex()
    .findAll(string)
    .map { match ->
      val commonStrMatch = match.groupValues[1]
      if (commonStrMatch.isNotEmpty()) {
        Quote(commonStrMatch, false)
      } else {
        Quote(match.groupValues[2], true, match.value)
      }
    }
    .toList()
