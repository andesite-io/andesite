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

package andesite.world.anvil

import andesite.world.Location
import andesite.world.World

private const val LoadFactor: Int = 4000000

public class AnvilWorld(public val regions: Array<AnvilRegion>) : World {
  public val chunks: MutableMap<Long, AnvilChunk> = HashMap<Long, AnvilChunk>().apply {
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
