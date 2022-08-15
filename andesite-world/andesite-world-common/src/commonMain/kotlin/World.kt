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

package andesite.world

/** Represents a generic Minecraft world. */
public interface World {
  /**
   * Tries to get a [Chunk] at the given [x] and [z] coordinates.
   *
   * @param x the x coordinate of the chunk
   * @param z the z coordinate of the chunk
   * @return if found, a [Chunk] at the given coordinates, otherwise null
   */
  public fun getChunkAt(x: Int, z: Int): Chunk?

  /**
   * Tries to get a [Chunk] at the given [location].
   *
   * @param location the location of the chunk
   * @return if found, a [Chunk] at the given coordinates, otherwise null
   */
  public fun getChunkAt(location: Location): Chunk?
}
