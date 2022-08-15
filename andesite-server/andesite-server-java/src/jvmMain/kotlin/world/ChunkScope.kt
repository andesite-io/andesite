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

package andesite.java.world

import andesite.player.MinecraftPlayer
import andesite.world.block.Block
import andesite.world.block.BlockState

interface ChunkScope {
  val player: MinecraftPlayer

  suspend fun getBlock(x: Int, y: Int, z: Int): BlockState

  suspend fun setBlock(x: Int, y: Int, z: Int, block: Block)

  suspend fun fill(y: Int, block: Block)
}

fun ChunkScope(player: MinecraftPlayer): ChunkScope {
  return ChunkScopeImpl(player)
}

private class ChunkScopeImpl(override val player: MinecraftPlayer) : ChunkScope {
  override suspend fun getBlock(x: Int, y: Int, z: Int): BlockState {
    TODO("Not yet implemented")
  }

  override suspend fun setBlock(x: Int, y: Int, z: Int, block: Block) {
    TODO("Not yet implemented")
  }

  override suspend fun fill(y: Int, block: Block) {
    TODO("Not yet implemented")
  }
}
