/*
 *    Copyright 2021 Gabrielle Guimarães de Oliveira
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

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Minecraft's text component wrapper.
 *
 * The [Chat] can be used as simple text.
 *
 * Example:
 * ```kt
 * Chat.of("&aHello, &bI &cam &dcolored!")
 * ```
 *
 * And as a full-featured component:
 * ```kt
 * Chat.build("{player} left the server") {
 *   val player by placeholder(player.username) {
 *   hoverEvent = ShowText("@${player.username}" * )
 *   hex("32a852")
 * }
 * ```
 *
 *
 * @param text the text to be displayed
 * @param bold whether the text should be bold
 * @param italic whether the text should be italic
 * @param underlined whether the text should be underlined
 * @param strikethrough whether the text should be strikethrough
 * @param obfuscated whether the text should be obfuscated
 * @param font the font to use
 * @param color the color of the text
 * @param insertion the insertion of the text
 * @param clickEvent the click event of the text
 * @param hoverEvent the hover event of the text
 * @param extra the extra text components contained
 */
@Serializable
@SerialName("Chat")
public data class Chat(
  public val text: String,
  public val bold: Boolean = false,
  public val italic: Boolean = false,
  public val underlined: Boolean = false,
  public val strikethrough: Boolean = false,
  public val obfuscated: Boolean = false,
  public val font: Identifier = Identifier("minecraft:default"),
  public val color: Color = Color.White,
  public val insertion: String? = null,
  public val clickEvent: ClickEvent? = null,
  public val hoverEvent: HoverEvent? = null,
  public val extra: List<Chat>? = null,
) {
  public companion object {
    public const val ColorCode: String = "\u00A7"

    /**
     * Gets a [Chat] object from a string converting color codes
     *
     * Example:
     * ```kt
     * Chat.of("&aHello, &bI &cam &dcolored!")
     * ```
     *
     * @param text the text to be converted
     * @return the [Chat] component
     */
    public fun of(text: String): Chat {
      return Chat(text.replace("&", ColorCode))
    }

    /**
     * Creates a full-featured [Chat] component.
     *
     * Example:
     * ```kt
     * Chat.build("{player} left the server") {
     *   val player by placeholder(player.username) {
     *   hoverEvent = ShowText("@${player.username}" * )
     *   hex("32a852")
     * }
     * ```
     *
     * @param text the base text
     * @param builder the builder function for the full-featured component
     * @return the [Chat] component
     */
    public fun build(text: String, builder: ChatBuilder.() -> Unit): Chat {
      return ChatBuilder(of(text)).apply(builder).build()
    }

    /**
     * Creates a full-featured [Chat] component.
     *
     * Example:
     * ```kt
     * val base = Chat.of("{player} left the server")
     *
     * Chat.build(base) {
     *   val player by placeholder(player.username) {
     *   hoverEvent = ShowText("@${player.username}" * )
     *   hex("32a852")
     * }
     * ```
     *
     * @param chat the base [Chat] component
     * @param builder the builder function for the full-featured component
     * @return the [Chat] component
     */
    public fun build(chat: Chat, builder: ChatBuilder.() -> Unit): Chat {
      return ChatBuilder(chat).apply(builder).build()
    }

    /**
     * Creates a list of [Chat] components. It can be useful for sending a batch of messages, like
     * in command usages, etc.
     *
     * Example:
     * ```kt
     * val chat = Chat.of("&7Use: /gamemode <gameMode>")
     *
     * Chat.many {
     *   append("Incorrect usage") {
     *     red()
     *   }
     *   append(chat)
     *   append("&cSee you!")
     * }
     * ```
     */
    public fun many(builder: ChatListBuilder.() -> Unit): List<Chat> {
      return ChatListBuilder().apply(builder).build()
    }
  }

  public fun with(chats: Collection<Chat>): Chat {
    return when {
      chats.isEmpty() -> this
      else -> copy(extra = extra.orEmpty().plus(chats))
    }
  }

  /**
   * Copy the [Chat] component with a new [HoverEvent].
   *
   * @param hoverEvent the new hover event
   * @return the new [Chat] component
   */
  public fun hoverEvent(hoverEvent: HoverEvent): Chat {
    return copy(hoverEvent = hoverEvent)
  }

  /**
   * Copy the [Chat] component with a new [ClickEvent].
   *
   * @param clickEvent the new click event
   * @return the new [Chat] component
   */
  public fun clickEvent(clickEvent: ClickEvent): Chat {
    return copy(clickEvent = clickEvent)
  }

  /**
   * Combine two [Chat] components.
   *
   * @param other the other [Chat] component
   * @return the combined [Chat] component
   */
  public operator fun plus(other: Chat): Chat {
    return copy(extra = extra.orEmpty().plusElement(other))
  }

  /**
   * Combine a [Chat] component with a [String].
   *
   * @param other the other [String]
   * @return the combined [Chat] component
   */
  public operator fun plus(other: String): Chat {
    return copy(text = text + other)
  }
}

public typealias PlaceholderProvider =
  PropertyDelegateProvider<Nothing?, ReadOnlyProperty<Nothing?, Chat>>

public class ChatListBuilder internal constructor() {
  private val components: MutableList<Chat> = mutableListOf()

  /**
   * Appends a [Chat] component to the list.
   *
   * @param chat the [Chat] component to be appended
   */
  public fun append(chat: Chat) {
    components += chat
  }

  /**
   * Appends a [Chat] component to the list.
   *
   * @param text the base text
   * @param builder the builder function for the full-featured component
   */
  public fun append(text: String, builder: ChatBuilder.() -> Unit = {}) {
    components += Chat.build(text, builder)
  }

  public fun build(): List<Chat> = components
}

public class ChatBuilder internal constructor(private val initial: Chat) {
  private val components: MutableList<Chat> = mutableListOf()
  private val placeholders: MutableMap<String, Chat> = mutableMapOf()

  public var clickEvent: ClickEvent? = initial.clickEvent
  public var hoverEvent: HoverEvent? = initial.hoverEvent
  public var insertion: String? = initial.insertion
  public var font: Identifier = initial.font
  public var color: Color = initial.color
  public var italic: Boolean = initial.italic
  public var strikethrough: Boolean = initial.strikethrough
  public var underlined: Boolean = initial.underlined
  public var obfuscated: Boolean = initial.obfuscated
  public var bold: Boolean = initial.bold

  /**
   * Appends a [Chat] component to the current component.
   *
   * @param text the base text
   * @param builder the builder function for the full-featured component
   */
  public fun append(text: String, builder: ChatBuilder.() -> Unit = {}) {
    components += Chat.build(text, builder)
  }

  /**
   * Appends a [Chat] component to the current component.
   *
   * @param chat the base [Chat] component
   * @param builder the builder function for the full-featured component
   */
  public fun append(chat: Chat, builder: ChatBuilder.() -> Unit = {}) {
    components += ChatBuilder(chat).apply(builder).build()
  }

  /**
   * Creates a simple placeholder.
   *
   * @param key the name of the placeholder
   * @param chat the [Chat] component of the placeholder
   * @return the [Chat] component
   */
  public fun placeholder(key: String, chat: Chat): Chat {
    placeholders[key] = chat
    return chat
  }

  /**
   * Creates a placeholder without the [PropertyDelegateProvider] feature.
   *
   * Example:
   * ```kt
   *  placeholder("player", player.username) {
   *    hoverEvent = ShowText("@${player.username}" * )
   *    hex("32a852")
   *  }
   * ```
   *
   * @param key the placeholder name
   * @param text the base text
   * @param builder the builder function for the full-featured component
   * @return the [Chat] component
   */
  public fun placeholder(key: String, text: String, builder: ChatBuilder.() -> Unit = {}): Chat {
    val chat = Chat.build(text, builder)
    placeholders[key] = chat
    return chat
  }

  /**
   * Creates a placeholder
   *
   * Example:
   * ```kt
   *  val player by placeholder(player.username) {
   *    hoverEvent = ShowText("@${player.username}" * )
   *    hex("32a852")
   *  }
   * ```
   *
   * @param text the base text
   * @param builder the builder function for the full-featured component
   * @return the delegating provider for the [Chat] component
   */
  public fun placeholder(text: String, builder: ChatBuilder.() -> Unit): PlaceholderProvider =
    PropertyDelegateProvider { _, property ->
      val chat = Chat.build(text, builder)

      placeholders[property.name] = chat

      ReadOnlyProperty { _, _ -> chat }
    }

  /**
   * Sets a HEX color code for the text.
   *
   * @param hex the HEX color code
   */
  public fun hex(hex: String) {
    color = HexColor(hex)
  }

  /** Sets the text as italic */
  public fun italic() {
    italic = true
  }

  /** Sets the text as strikethrough */
  public fun strikethrough() {
    strikethrough = true
  }

  /** Sets the text as underlined */
  public fun underlined() {
    underlined = true
  }

  /** Sets the text as obfuscated */
  public fun obfuscated() {
    obfuscated = true
  }

  /** Sets the text as bold */
  public fun bold() {
    bold = true
  }

  /** Sets the text as black colored. */
  public fun black() {
    color = Color.Black
  }

  /** Sets the text as dark blue colored. */
  public fun darkBlue() {
    color = Color.DarkBlue
  }

  /** Sets the text as dark green colored. */
  public fun darkGreen() {
    color = Color.DarkGreen
  }

  /** Sets the text as dark cyan colored. */
  public fun darkCyan() {
    color = Color.DarkCyan
  }

  /** Sets the text as dark red colored. */
  public fun darkRed() {
    color = Color.DarkRed
  }

  /** Sets the text as purple colored. */
  public fun purple() {
    color = Color.Purple
  }

  /** Sets the text as gold colored. */
  public fun gold() {
    color = Color.Gold
  }

  /** Sets the text as gray colored. */
  public fun gray() {
    color = Color.Gray
  }

  /** Sets the text as dark gray colored. */
  public fun darkGray() {
    color = Color.DarkGray
  }

  /** Sets the text as blue colored. */
  public fun blue() {
    color = Color.Blue
  }

  /** Sets the text as bright green colored. */
  public fun brightGreen() {
    color = Color.BrightGreen
  }

  /** Sets the text as cyan colored. */
  public fun cyan() {
    color = Color.Cyan
  }

  /** Sets the text as red colored. */
  public fun red() {
    color = Color.Red
  }

  /** Sets the text as pink colored. */
  public fun pink() {
    color = Color.Pink
  }

  /** Sets the text as yellow colored. */
  public fun yellow() {
    color = Color.Yellow
  }

  /** Sets the text as white colored. */
  public fun white() {
    color = Color.White
  }

  /**
   * Builds the [Chat] component.
   *
   * @return the [Chat] component
   */
  public fun build(): Chat {
    val chat = initial.copy(
      hoverEvent = hoverEvent,
      clickEvent = clickEvent,
      font = font,
      color = color,
      italic = italic,
      bold = bold,
      strikethrough = strikethrough,
      underlined = underlined,
      obfuscated = obfuscated,
      insertion = insertion,
    )

    val texts = quoteString(chat.text).map { quote ->
      when (quote.placeholder) {
        true -> placeholders[quote.text] ?: chat.copy(text = quote.fullText, extra = null)
        false -> chat.copy(text = quote.fullText, extra = null)
      }
    }

    return chat.copy(text = "").with(texts).with(components)
  }
}
