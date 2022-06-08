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

internal fun quoteString(string: String): List<Quote> = buildList {
  fun addIfNotEmpty(i: Int, j: Int, placeholder: Boolean = false) {
    val text = string.substring(i, j)
    if (text.isEmpty()) return

    add(Quote(text, placeholder))
  }

  var i = 0
  var j = 0
  var escaping = false

  while (j < string.length) {
    val c = string[j]

    if (!escaping && c == '{') {
      var k = i + 1
      while (string[k] != '}') {
        k++
      }
      k++

      val placeholder = string.substring(j + 1, k - 1)
      if (placeholder.isNotEmpty()) {
        addIfNotEmpty(i, j)
        add(Quote(placeholder, true, string.substring(j, k)))
        i = k
      } else {
        j++
      }
    }

    escaping = false

    if (c == '\\') {
      escaping = true
    }

    j++
  }

  addIfNotEmpty(i, j)
}
