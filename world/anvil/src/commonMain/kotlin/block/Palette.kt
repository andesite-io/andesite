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

package com.gabrielleeg1.andesite.api.world.anvil.block

import com.gabrielleeg1.andesite.api.protocol.types.VarInt
import com.gabrielleeg1.andesite.api.protocol.writeVarInt
import com.gabrielleeg1.andesite.api.world.block.Block
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.buildPacket

sealed interface Palette {
  val bitsPerBlock: Int
  
  fun stateIdForBlock(block: Block): StateId?
  
  fun writeToNetwork(): ByteReadPacket
}

class SingleValuePalette(val singleStateId: StateId) : Palette {
  override val bitsPerBlock: Int = 0
  
  override fun stateIdForBlock(block: Block): StateId? {
    TODO("Not yet implemented")
  }

  override fun writeToNetwork(): ByteReadPacket = buildPacket {
    writeVarInt(singleStateId)
  }
}

/**
 * The indirect palette have three variants:
 *   - For block states and bits per entry <= 4, 4 bits are used to represent a block
 *   - For block states and bits per between 5 and 8, the given value is used
 *   - For biomes and bits per entry <= 3, the given value is used
 */
class IndirectPalette(override val bitsPerBlock: Int, val palette: Array<VarInt>) : Palette {
  override fun stateIdForBlock(block: Block): StateId? {
    TODO("Not yet implemented")
  }

  override fun writeToNetwork(): ByteReadPacket = buildPacket {
    writeVarInt(palette.size)
    for (item in palette) {
      writeVarInt(item)
    }
  }
}

/**
 * This format is used for bits per entry values greater than or equal to a threshold (9 for block states, 4 for biomes)
 */
class DirectPalette(val globalPalette: GlobalPalette) : Palette {
  override val bitsPerBlock: Int = globalPalette.bitsPerBlock
  
  override fun stateIdForBlock(block: Block): StateId? {
    return globalPalette.stateIdForBlock(block)
  }

  override fun writeToNetwork(): ByteReadPacket = buildPacket { }
}
