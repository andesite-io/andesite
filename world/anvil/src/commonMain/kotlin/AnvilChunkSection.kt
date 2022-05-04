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

import andesite.world.ChunkSection
import andesite.world.anvil.block.BlockRegistry
import andesite.world.anvil.block.PalettedContainer
import andesite.world.anvil.block.directPalette
import andesite.world.anvil.block.readBlockPalette
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.writeFully
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import net.benwoodworth.knbt.NbtCompound

class AnvilChunkSection(
  val y: Int,
  val skyLight: ByteArray,
  val blockLight: ByteArray,
  val blockStates: PalettedContainer,
) : ChunkSection {
  val bitsPerBlock = blockStates.bitsPerBlock
  val sectionHeight = 16
  val sectionWidth = 16

  val serializedSize: Int
    get(): Int = 2 + blockStates.serializedSize

  fun isEmpty(): Boolean {
    return blockStates.nonEmptyBlockCount == 0.toShort()
  }

  @OptIn(ExperimentalUnsignedTypes::class)
  override fun writeToNetwork(): ByteReadPacket = buildPacket {
    writeFully(blockStates.writeToNetwork().readBytes())
  }
}

class AnvilChunkSectionSerializer(
  private val registry: BlockRegistry,
) : KSerializer<AnvilChunkSection> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AnvilChunkSection") {
    element<Byte>("Y")
    element<ByteArray>("SkyLight")
    element<ByteArray>("BlockLight")
    element<LongArray>("BlockStates")
    element<List<NbtCompound>>("Palette")
  }

  override fun serialize(encoder: Encoder, value: AnvilChunkSection) {
    TODO("Not yet implemented")
  }

  override fun deserialize(decoder: Decoder): AnvilChunkSection {
    return decoder.decodeStructure(descriptor) {
      var y: Int = -1
      var skyLight = ByteArray(1024 * 2)
      var blockLight = ByteArray(1024 * 2)
      var blockStates: LongArray? = null
      var palette: List<NbtCompound>? = null

      while (true) {
        when (val index = decodeElementIndex(descriptor)) {
          0 -> y = decodeByteElement(descriptor, index).toInt()
          1 -> skyLight = decodeSerializableElement(descriptor, index, ByteArraySerializer())
          2 -> blockLight = decodeSerializableElement(descriptor, index, ByteArraySerializer())
          3 -> blockStates = decodeSerializableElement(descriptor, index, LongArraySerializer())
          4 -> palette = decodeSerializableElement(
            descriptor = descriptor,
            index = 3,
            deserializer = ListSerializer(NbtCompound.serializer()),
          )
          CompositeDecoder.DECODE_DONE -> break
          else -> error("Unexpected index: $index")
        }
      }
      
      val blocks = when {
        blockStates == null -> directPalette(registry)
        palette == null -> directPalette(registry)
        else -> readBlockPalette(registry, blockStates, palette)
      }

      AnvilChunkSection(y, skyLight, blockLight, blocks)
    }
  }
}
