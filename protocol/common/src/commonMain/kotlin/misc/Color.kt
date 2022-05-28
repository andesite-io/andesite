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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(ColorSerializer::class)
@SerialName("Color")
public sealed class Color {
  public abstract val text: String

  public companion object {
    public val Black: Color = MinecraftColor("0")
    public val DarkBlue: Color = MinecraftColor("1")
    public val DarkGreen: Color = MinecraftColor("2")
    public val DarkCyan: Color = MinecraftColor("3")
    public val DarkRed: Color = MinecraftColor("4")
    public val Purple: Color = MinecraftColor("5")
    public val Gold: Color = MinecraftColor("6")
    public val Gray: Color = MinecraftColor("7")
    public val DarkGray: Color = MinecraftColor("8")
    public val Blue: Color = MinecraftColor("9")
    public val BrightGreen: Color = MinecraftColor("a")
    public val Cyan: Color = MinecraftColor("b")
    public val Red: Color = MinecraftColor("c")
    public val Pink: Color = MinecraftColor("d")
    public val Yellow: Color = MinecraftColor("e")
    public val White: Color = MinecraftColor("f")
  }
}

@Serializable
public class MinecraftColor(override val text: String) : Color()

@Serializable
public class HexColor(override val text: String) : Color()

internal object ColorSerializer : KSerializer<Color> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Color) {
    encoder.encodeString(value.text)
  }

  override fun deserialize(decoder: Decoder): Color {
    val string = decoder.decodeString()

    return when (string.startsWith("#")) {
      true -> HexColor(string)
      false -> MinecraftColor(string)
    }
  }
}
