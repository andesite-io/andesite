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

package com.gabrielleeg1.andesite.api.protocol.serializers

import com.benasher44.uuid.Uuid
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object UuidSerializer : KSerializer<Uuid> {
  override val descriptor =
    buildClassSerialDescriptor("com.gabrielleeg1.andesite.api.protocol.serializers.Uuid") {
      element<Long>("mostSignificantBits")
      element<Long>("leastSignificantBits")
    }

  override fun serialize(encoder: Encoder, value: Uuid) {
    encoder.encodeLong(value.mostSignificantBits)
    encoder.encodeLong(value.leastSignificantBits)
  }

  override fun deserialize(decoder: Decoder): Uuid {
    return Uuid(decoder.decodeLong(), decoder.decodeLong())
  }
}
