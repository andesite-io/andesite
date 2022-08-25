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

package andesite.protocol.java.command

import andesite.protocol.misc.Identifier
import andesite.protocol.types.VarInt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.benwoodworth.knbt.NbtCompound

@Serializable
public data class CommandNode(
  public val flags: Byte,
  public val children: List<VarInt>,
  public val redirectNode: VarInt?,
  public val name: String?,
  public val parserId: String?,
  public val properties: NbtCompound?,

  @SerialName("suggestionsType")
  public val suggestionKind: Identifier,
)
