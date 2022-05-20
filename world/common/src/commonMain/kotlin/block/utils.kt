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

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import net.benwoodworth.knbt.NbtByte
import net.benwoodworth.knbt.NbtByteArray
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtDouble
import net.benwoodworth.knbt.NbtFloat
import net.benwoodworth.knbt.NbtInt
import net.benwoodworth.knbt.NbtIntArray
import net.benwoodworth.knbt.NbtList
import net.benwoodworth.knbt.NbtLong
import net.benwoodworth.knbt.NbtLongArray
import net.benwoodworth.knbt.NbtShort
import net.benwoodworth.knbt.NbtString
import net.benwoodworth.knbt.NbtTag
import net.benwoodworth.knbt.buildNbtCompound

fun namelessTag(tag: NbtTag): NbtCompound {
  return buildNbtCompound { put("", tag) }
}

fun NbtCompound.toJsonObject(): JsonObject = this.toJsonElement().jsonObject

fun NbtTag.toJsonElement(): JsonElement {
  return when (this) {
    is NbtCompound -> buildJsonObject {
      forEach { (key, value) -> put(key, value.toJsonElement()) }
    }
    is NbtList<*> -> buildJsonArray {
      forEach { add(it.toJsonElement()) }
    }
    is NbtFloat -> JsonPrimitive(value)
    is NbtInt -> JsonPrimitive(value)
    is NbtByte -> JsonPrimitive(value)
    is NbtShort -> JsonPrimitive(value)
    is NbtLong -> JsonPrimitive(value)
    is NbtDouble -> JsonPrimitive(value)
    is NbtString -> JsonPrimitive(value)
    is NbtByteArray -> JsonArray(map(::JsonPrimitive))
    is NbtIntArray -> JsonArray(map(::JsonPrimitive))
    is NbtLongArray -> JsonArray(map(::JsonPrimitive))
  }
}
