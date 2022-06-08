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

import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

class QuoteTest {
  @Test
  fun `test parse placeholder empty`() {
    val expected = listOf(Quote("{} doing", false))
    val actual = quoteString("{} doing")

    assertEquals(expected, actual)
  }

  @Test
  fun `test parse placeholder with text behind placeholder`() {
    val expected = listOf(
      Quote("hello ", false),
      Quote("player", true, "{player}"),
      Quote(" doing", false),
    )
    val actual = quoteString("hello {player} doing")

    assertEquals(expected, actual)
  }

  @Test
  fun `test parse placeholder`() {
    val expected = listOf(
      Quote("player", true, "{player}"),
      Quote(" doing", false),
    )
    val actual = quoteString("{player} doing")

    assertEquals(expected, actual)
  }
}
