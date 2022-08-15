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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.serializer

/** [Chat] component's hover event. */
@Serializable(HoverEventSerializer::class)
@SerialName("HoverEvent")
public sealed class HoverEvent

/**
 * Shows a [chat] when hovering.
 *
 * @param chat the [Chat] component to show
 */
@Serializable
public data class ShowText(val chat: Chat) : HoverEvent() {
  public constructor(text: String) : this(Chat.of(text))
}

// TODO: support the other hover events

internal object HoverEventSerializer : KSerializer<HoverEvent> {
  @Serializable
  private class Surrogate<T : Any>(val action: String, val value: T)

  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("HoverEvent") {
    element<String>("action")
    element<JsonElement>("value")
  }

  override fun serialize(encoder: Encoder, value: HoverEvent) {
    when (value) {
      is ShowText -> encoder.encodeSurrogate("show_text", value.chat)
    }
  }

  override fun deserialize(decoder: Decoder): HoverEvent {
    require(decoder is JsonDecoder)

    val json = decoder.json
    val surrogate = decoder.decodeSerializableValue(Surrogate.serializer(JsonElement.serializer()))

    return when (surrogate.action) {
      "show_text" -> when (val value = surrogate.value) {
        is JsonPrimitive -> ShowText(json.decodeFromJsonElement<String>(surrogate.value))
        is JsonObject -> ShowText(json.decodeFromJsonElement<Chat>(value))
        else -> error("Unsupported value for 'show_text' action in HoverEvent!")
      }

      else -> error("Unsupported action '${surrogate.action}' for HoverEvent!")
    }
  }

  private inline fun <reified A : Any> Encoder.encodeSurrogate(action: String, value: A) {
    val surrogate = Surrogate(action, value)
    encodeSerializableValue(Surrogate.serializer(serializer()), surrogate)
  }
}
