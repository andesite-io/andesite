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

import com.github.ajalt.mordant.rendering.TextStyle

/**
 * Converts a [Chat] component into a [String] that have color information using Mordant.
 *
 * @receiver the [Chat] component to be converted
 * @return the [String] with color information
 */
public fun Chat.mordant(): String {
  return flatten().joinToString("") { chat ->
    val text = when (chat.obfuscated) {
      true -> chat.text.map { ' ' }.toCharArray().concatToString()
      false -> chat.text
    }

    val style = TextStyle(
      color = when (val color = chat.color) {
        Color.White -> null
        else -> color.style.color
      },
      bold = chat.bold,
      italic = chat.italic,
      underline = chat.underlined,
      strikethrough = chat.strikethrough,
    )

    style(text)
  }
}

/**
 * Converts a [Chat] component with many [Chat.extra] children into a [List] of [Chat].
 *
 * @receiver the [Chat] component to be converted
 * @return the [List] of [Chat]
 */
public fun Chat.flatten(): List<Chat> {
  if (extra.isNullOrEmpty()) return listOf(this)

  return extra.fold(listOf(this)) { a, b ->
    a + b.flatten()
  }
}
