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

package andesite.item

import andesite.item.properties.FoodProperties
import andesite.protocol.misc.Identifier
import kotlinx.serialization.Serializable

@Serializable
public data class Item(
  val id: Long,
  val mojangName: String,
  val rarity: Rarity,
  val translationKey: String,
  val depletes: Boolean,
  val maxStackSize: Long,
  val maxDamage: Long,
  val edible: Boolean,
  val fireResistant: Boolean,
  val blockId: Identifier,
  val eatingSound: Identifier,
  val drinkingSound: Identifier,
  val specificItemData: SpecificItemData,
  val foodProperties: FoodProperties? = null,
)
