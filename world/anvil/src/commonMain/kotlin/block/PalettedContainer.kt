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

package andesite.world.anvil.block

import andesite.protocol.countVarInt
import andesite.protocol.types.VarInt
import andesite.protocol.writeVarInt
import andesite.world.anvil.BitStorage
import andesite.world.block.Block
import andesite.world.block.BlockRegistry
import andesite.world.block.toBlock
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.isNotEmpty
import io.ktor.utils.io.core.writeFully
import io.ktor.utils.io.core.writeShort
import io.ktor.utils.io.core.writeUByte
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.max
import net.benwoodworth.knbt.NbtCompound

public class PalettedContainer(public val palette: Palette, public val storage: BitStorage) {
  public val bitsPerBlock: Int = palette.bitsPerBlock
  public val blocks: List<Block> = storage
    .iterator()
    .asSequence()
    .mapNotNull { palette.blockById(it) }
    .toList()
  public var nonEmptyBlockCount: Short = 0
    private set

  public val serializedSize: Int
    get() = 1 + palette.serializedSize + storage.size.countVarInt() + storage.data.size * 8

  public fun blockOf(x: Int, y: Int, z: Int): Block {
    return blocks[(y and 0xF) * 256 + (z and 0xF) * 16 + (x and 0xF)]
  }

  public fun recount() {
    nonEmptyBlockCount = blocks.filterNot { it.isAir }.count().toShort()
  }

  public fun writeToNetwork(): ByteReadPacket = buildPacket {
    writeShort(nonEmptyBlockCount)
    writeUByte(palette.bitsPerBlock.toUByte())
    val palette = palette.writeToNetwork()
    if (palette.isNotEmpty) {
      writePacket(palette)
    }
    writeVarInt(storage.data.size)
    writeFully(storage.data)
  }
}

internal fun directPalette(registry: BlockRegistry): PalettedContainer {
  return PalettedContainer(DirectPalette(registry), BitStorage.empty()).apply {
    recount()
  }
}

@Suppress("UNUSED_PARAMETER")
internal fun readBlockPalette(
  registry: BlockRegistry,
  blockStates: LongArray,
  blockPalette: List<NbtCompound>,
): PalettedContainer {
  val bits = max(4.0, log2(ceil(blockPalette.size.toDouble()))).toInt()

  val size = 1 shl 4 * 3

  val palette = when {
    bits == 0 -> {
      val palette = SingleValuePalette(registry.stateIdForBlock(blockPalette.first().toBlock())!!)

      PalettedContainer(palette, BitStorage.empty())
    }
    bits in 1..8 -> {
      val palette = IndirectPalette(
        bits,
        registry,
        blockPalette
          .map { it.toBlock() }
          .map { registry.stateIdForBlock(it) ?: error("Could not find id for block $it") }
          .map { VarInt(it) }
          .toTypedArray(),
      )

      PalettedContainer(palette, BitStorage(bits, size, blockStates))
    }
    bits >= 9 -> {
      val ids = IntArray(size).also { ids ->
        BitStorage(bits, ids.size, blockStates).unpack(ids)
      }

      val storage = BitStorage(bits, ids.size, ids)

      PalettedContainer(DirectPalette(registry), storage)
    }
    else -> error("Can not get palette from $bits bits")
  }

  return palette.apply { recount() }
}
