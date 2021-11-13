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

package com.gabrielleeg1.javarock.api.world.anvil

import com.gabrielleeg1.javarock.api.world.Location
import com.gabrielleeg1.javarock.api.world.World

private const val LoadFactor = 4000000

class AnvilWorld(val regions: Array<AnvilRegion>) : World {
  val chunks = HashMap<Long, AnvilChunk>().apply {
    regions.flatMap(AnvilRegion::chunks).forEach {
      put((it.x * LoadFactor + it.z).toLong(), it)
    }
  }

  override fun getChunkAt(x: Int, z: Int): AnvilChunk? {
    return chunks[(x * LoadFactor + z).toLong()]
  }

  override fun getChunkAt(location: Location): AnvilChunk? {
    return null
  }
}
