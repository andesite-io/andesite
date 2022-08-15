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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Identifiers are a namespaced location, in the form of minecraft:thing. If the namespace is not
 * provided, it defaults to minecraft (i.e. thing is minecraft:thing). Custom content should always
 * be in its own namespace, not the default one. Both the namespace and value can use all lowercase
 * alphanumeric characters (a-z and 0-9), dot (.), dash (-), and underscore (_). In addition, values
 * can use slash (/). The naming convention is lower_case_with_underscores.
 *
 * @param fullPath the full path of the identifier
 */
@Serializable(IdentifierSerializer::class)
public class Identifier(public val fullPath: String) {
  public val namespace: String = fullPath.substringBefore(':', "minecraft")
  public val path: String = fullPath.substringAfter(':')

  public constructor(namespace: String, path: String) : this("$namespace:$path")

  override fun hashCode(): Int = fullPath.hashCode()
  override fun toString(): String = fullPath

  override fun equals(other: Any?): Boolean {
    if (other is Identifier) {
      return fullPath == other.fullPath
    }

    return false
  }
}

internal object IdentifierSerializer : KSerializer<Identifier> {
  override val descriptor = PrimitiveSerialDescriptor("Identifier", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): Identifier {
    return Identifier(decoder.decodeString())
  }

  override fun serialize(encoder: Encoder, value: Identifier) {
    encoder.encodeString(value.toString())
  }
}
