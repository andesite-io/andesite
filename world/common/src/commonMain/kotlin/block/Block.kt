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

package andesite.world.block

import andesite.protocol.misc.Identifier
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtString
import net.benwoodworth.knbt.buildNbtCompound

data class Block(
  val id: Identifier,
  val properties: JsonObject = buildJsonObject {  },
) {
  val isAir: Boolean get() = id.equals("minecraft:air")
}

fun NbtCompound.toBlock(): Block {
  val id = get("Name") as NbtString? ?: NbtString("minecraft:air")
  val properties = get("Properties") as NbtCompound? ?: buildNbtCompound {  }
  
  return Block(Identifier(id.value), properties.toJsonObject())
}
