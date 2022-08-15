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

import com.github.ajalt.mordant.rendering.TextColors.Companion.rgb
import com.github.ajalt.mordant.rendering.TextStyle
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Minecraft's color component wrapper.
 */
@Serializable(ColorSerializer::class)
@SerialName("Color")
public sealed class Color {
  /** The color text. */
  public abstract val text: String

  /** The text style to be represented in a terminal context with Mordant. */
  public abstract val style: TextStyle

  public companion object {
    public val Black: Color = MinecraftColor("0", rgb("#000000"))
    public val DarkBlue: Color = MinecraftColor("1", rgb("#0000aa"))
    public val DarkGreen: Color = MinecraftColor("2", rgb("#00aa00"))
    public val DarkCyan: Color = MinecraftColor("3", rgb("#00aaaa"))
    public val DarkRed: Color = MinecraftColor("4", rgb("#aa0000"))
    public val Purple: Color = MinecraftColor("5", rgb("#aa00aa"))
    public val Gold: Color = MinecraftColor("6", rgb("#ffaa00"))
    public val Gray: Color = MinecraftColor("7", rgb("#aaaaaa"))
    public val DarkGray: Color = MinecraftColor("8", rgb("#555555"))
    public val Blue: Color = MinecraftColor("9", rgb("#5555ff"))
    public val BrightGreen: Color = MinecraftColor("a", rgb("#55ff55"))
    public val Cyan: Color = MinecraftColor("b", rgb("#55ffff"))
    public val Red: Color = MinecraftColor("c", rgb("#ff5555"))
    public val Pink: Color = MinecraftColor("d", rgb("#ff55ff"))
    public val Yellow: Color = MinecraftColor("e", rgb("#ffff55"))
    public val White: Color = MinecraftColor("f", rgb("#ffffff"))
  }
}

/**
 * Minecraft default color.
 *
 * @param text the color text.
 * @param style the text style to be represented in a terminal context with Mordant.
 */
@Serializable
public data class MinecraftColor(override val text: String, override val style: TextStyle) : Color()

/**
 * Minecraft color with a HEX value.
 *
 * @param text the HEX color value.
 */
@Serializable
public data class HexColor(override val text: String) : Color() {
  init {
    require(!text.startsWith("#")) { "The hex color must not start with `#`" }
  }

  override val style: TextStyle = rgb("#$text")
}

internal object ColorSerializer : KSerializer<Color> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Color) {
    when (value) {
      is MinecraftColor -> encoder.encodeString(value.text)
      is HexColor -> encoder.encodeString("#${value.text}")
    }
  }

  override fun deserialize(decoder: Decoder): Color {
    val string = decoder.decodeString()
    if (string.startsWith("#")) return HexColor(string)

    return when (string) {
      "1" -> Color.Black
      "2" -> Color.DarkBlue
      "3" -> Color.DarkCyan
      "4" -> Color.DarkRed
      "5" -> Color.Purple
      "6" -> Color.Gold
      "7" -> Color.Gray
      "8" -> Color.DarkGray
      "9" -> Color.Blue
      "a" -> Color.BrightGreen
      "b" -> Color.Cyan
      "c" -> Color.Red
      "d" -> Color.Pink
      "e" -> Color.Yellow
      "f" -> Color.White
      else -> error("Unsupported minecraft color: $string")
    }
  }
}
