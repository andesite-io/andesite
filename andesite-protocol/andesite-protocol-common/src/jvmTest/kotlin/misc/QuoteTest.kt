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

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class QuoteTest {
  @Test
  fun `test parse empty placeholder`() {
    val expected = listOf(Quote("Hello {}!", false))
    val actual = quoteString("Hello {}!")

    assertEquals(expected, actual)
  }

  @Test
  fun `test parse placeholder with text before`() {
    val expected = listOf(
      Quote("Hello ", false),
      Quote("player", true, "{player}"),
    )
    val actual = quoteString("Hello {player}")

    assertEquals(expected, actual)
  }

  @Test
  fun `test parse placeholder with text after`() {
    val expected = listOf(
      Quote("player", true, "{player}"),
      Quote(", hi!", false),
    )
    val actual = quoteString("{player}, hi!")

    assertEquals(expected, actual)
  }

  @Test
  fun `test parse placeholder with text before and after`() {
    val expected = listOf(
      Quote("Hello ", false),
      Quote("player", true, "{player}"),
      Quote(", welcome!", false),
    )
    val actual = quoteString("Hello {player}, welcome!")

    assertEquals(expected, actual)
  }

  @Test
  fun `test parse placeholder with escaped curly braces`() {
    val expected = listOf(
      Quote("Hello ", false),
      Quote("player", true, "{player}"),
      Quote(", welcome to \\{our server\\}!", false),
    )
    val actual = quoteString("Hello {player}, welcome to \\{our server\\}!")

    assertEquals(expected, actual)
  }
}
