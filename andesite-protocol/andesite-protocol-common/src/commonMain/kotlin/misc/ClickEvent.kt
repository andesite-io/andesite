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
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.serializer

/** [Chat] component's click event. */
@Serializable(ClickEventSerializer::class)
@SerialName("ClickEvent")
public sealed class ClickEvent

/**
 * Opens a URL for the player.
 *
 * @param url the URL to open
 */
@Serializable
public data class OpenUrl(val url: String) : ClickEvent()

/**
 * Runs a command as the player.
 *
 * @param command the command to run
 */
@Serializable
public data class RunCommand(val command: String) : ClickEvent()

/**
 * Suggets a command to the player.
 *
 * @param command the command to suggest
 */
@Serializable
public data class SuggestCommand(val command: String) : ClickEvent()

/**
 * Changes the page of the current open book.
 *
 * @param page the page to change to
 */
@Serializable
public data class ChangePage(val page: Int) : ClickEvent()

/**
 * Copies a [text] to the player's clipboard.
 *
 * @param text the text to copy
 */
@Serializable
public data class CopyToClipboard(val text: String) : ClickEvent()

internal object ClickEventSerializer : KSerializer<ClickEvent> {
  @Serializable
  private class Surrogate<T : Any>(val action: String, val value: T)

  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("HoverEvent") {
    element<String>("action")
    element<JsonElement>("value")
  }

  override fun serialize(encoder: Encoder, value: ClickEvent) {
    when (value) {
      is OpenUrl -> encoder.encodeSurrogate("open_url", value.url)
      is RunCommand -> encoder.encodeSurrogate("run_command", value.command)
      is SuggestCommand -> encoder.encodeSurrogate("suggest_command", value.command)
      is ChangePage -> encoder.encodeSurrogate("change_page", value.page)
      is CopyToClipboard -> encoder.encodeSurrogate("copy_to_clipboard", value.text)
    }
  }

  override fun deserialize(decoder: Decoder): ClickEvent {
    require(decoder is JsonDecoder)

    val json = decoder.json
    val surrogate = decoder.decodeSerializableValue(Surrogate.serializer(JsonElement.serializer()))

    return when (surrogate.action) {
      "open_url" -> OpenUrl(json.decodeFromJsonElement(surrogate.value))
      "run_command" -> RunCommand(json.decodeFromJsonElement(surrogate.value))
      "suggest_command" -> SuggestCommand(json.decodeFromJsonElement(surrogate.value))
      "change_page" -> ChangePage(json.decodeFromJsonElement(surrogate.value))
      "copy_to_clipboard" -> RunCommand(json.decodeFromJsonElement(surrogate.value))
      else -> error("Unsupported action '${surrogate.action}' for HoverEvent!")
    }
  }

  private inline fun <reified A : Any> Encoder.encodeSurrogate(action: String, value: A) {
    val surrogate = Surrogate(action, value)
    encodeSerializableValue(Surrogate.serializer(serializer()), surrogate)
  }
}
