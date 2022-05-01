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

@file:OptIn(ExperimentalUnsignedTypes::class)

package com.gabrielleeg1.andesite.api.world.anvil.block

import com.gabrielleeg1.andesite.api.protocol.countVarInt
import com.gabrielleeg1.andesite.api.protocol.writeVarInt
import com.gabrielleeg1.andesite.api.world.block.Block
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.isNotEmpty
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.writeFully
import io.ktor.utils.io.core.writeShort
import io.ktor.utils.io.core.writeUByte
import net.benwoodworth.knbt.NbtCompound

class PalettedContainer(
  val palette: Palette,
  val data: LongArray = LongArray(((16 * 16 * 16) * palette.bitsPerBlock) / (64 * palette.bitsPerBlock)) {
    0
  },
) {
  val bitsPerBlock = palette.bitsPerBlock
  val blocks = mutableListOf<Block>()
  var nonEmptyBlockCount: Short = 0
    private set
  
  val serializedSize: Int get() = 1 + palette.serializedSize + data.size.countVarInt() + data.size * 8

  fun blockOf(x: Int, y: Int, z: Int): Block {
    return blocks[(y and 0xF) * 256 + (z and 0xF) * 16 + (x and 0xF)]
  }

  fun recount() {
    nonEmptyBlockCount = blocks.filterNot { it.isAir }.count().toShort()
  }

  fun writeToNetwork(): ByteReadPacket = buildPacket {
    writeShort(nonEmptyBlockCount)
    writeUByte(palette.bitsPerBlock.toUByte())
    val palette = palette.writeToNetwork()
    if (palette.isNotEmpty) {
      writePacket(palette)
    }
    println("SIZE ${data.size}")
    writeVarInt(data.size)
    writeFully(data)
  }
}

internal fun directPalette(globalPalette: GlobalPalette): PalettedContainer {
  return PalettedContainer(DirectPalette(globalPalette)).apply {
    recount()
  }
}

@Suppress("UNUSED_PARAMETER")
internal fun readBlockPalette(
  globalPalette: GlobalPalette,
  blockStates: LongArray,
  palette: List<NbtCompound>,
): PalettedContainer {
//  todo implement palettes
//  val bitsPerBlock = max(4.0, log2(ceil(palette.size.toDouble()))).toInt()
//  val palette = when {
//    bitsPerBlock <= 4 -> TODO()
//    bitsPerBlock in 5..8 -> IndirectPalette(
//      bitsPerBlock,
//      palette
//        .map { it.toBlock() }
//        .map { globalPalette.idForBlock(it) ?: error("Could not find state id for block $it") }
//        .map { VarInt(it) }
//        .toTypedArray(),
//    )
//    bitsPerBlock == 9 -> DirectPalette(bitsPerBlock)
//    else -> error("Can not get palette from $bitsPerBlock bits")
//  }

  return directPalette(globalPalette)
}
