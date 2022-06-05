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

package andesite.world.anvil

public interface BitStorage {
  public val bits: Int
  public val size: Int
  public val data: LongArray

  public operator fun get(index: Int): Int
  public operator fun set(index: Int, value: Int)

  public fun getAndSet(index: Int, value: Int): Int
  public fun unpack(out: IntArray)
  public fun forEachIndexed(f: (index: Int, value: Int) -> Unit)

  public fun iterator(): IntIterator

  public companion object {
    public fun empty(): BitStorage = ZeroBitStorage
  }
}

public fun BitStorage(bits: Int, size: Int, data: LongArray? = null): BitStorage {
  return BitStorageImpl(bits, size, data)
}

public fun BitStorage(bits: Int, size: Int, values: IntArray): BitStorage {
  return BitStorageImpl(bits, size, values)
}

private object ZeroBitStorage : BitStorage {
  override val bits: Int = 0
  override val size: Int = 0
  override val data: LongArray = LongArray(0)

  override fun get(index: Int): Int {
    require(index in 0 until size) { "Index $index is out of bounds" }
    return 0
  }

  override fun set(index: Int, value: Int) {
    require(index in 0 until size) { "Index $index is out of bounds" }
  }

  override fun getAndSet(index: Int, value: Int): Int {
    require(index in 0 until size) { "Index $index is out of bounds" }
    return 0
  }

  override fun unpack(out: IntArray) {
    out.fill(0)
  }

  override fun forEachIndexed(f: (index: Int, value: Int) -> Unit) {
    // nothing to do
  }

  override fun iterator(): IntIterator {
    return IntArray(0).iterator()
  }
}

private class BitStorageImpl(
  override val bits: Int,
  override val size: Int,
  data: LongArray? = null,
) : BitStorage {
  override val data: LongArray

  val divideMultiply: Long
  val divideAdd: Long
  val divideShift: Int

  val mask = (1L shl bits) - 1L
  val valuesPerLong = Long.SIZE_BITS / bits

  init {
    val magicIndex = 3 * (valuesPerLong - 1)

    divideMultiply = MAGIC[magicIndex].toLong() and 0xffffffffL
    divideAdd = MAGIC[magicIndex + 1].toLong() and 0xffffffffL
    divideShift = MAGIC[magicIndex + 2]

    val dataSize = (size + valuesPerLong - 1) / valuesPerLong
    when {
      data != null && data.size != dataSize -> {
        error("Invalid data length, expecting $dataSize, but got ${data.size}")
      }
      data != null -> this.data = data
      else -> this.data = LongArray(dataSize)
    }
  }

  constructor(bits: Int, size: Int, values: IntArray) : this(bits, size) {
    var cellIndex = 0
    var dataIndex = 0

    while (dataIndex <= size - valuesPerLong) {
      var datum = 0L

      for (i in valuesPerLong - 1 downTo 0) {
        datum = datum shl bits
        datum = datum or (values[cellIndex + i].toLong() and mask)
      }

      data[cellIndex++] = datum
      dataIndex += valuesPerLong
    }

    val remaining = size - dataIndex
    if (remaining > 0) {
      var datum = 0L

      for (i in remaining - 1 downTo 0) {
        datum = datum shl bits
        datum = datum or (values[cellIndex + i].toLong() and mask)
      }

      data[cellIndex] = datum
    }
  }

  override operator fun set(index: Int, value: Int) {
    getAndSet(index, value)
  }

  override operator fun get(index: Int): Int {
    require(index in 0 until size) { "Index $index is out of bounds" }

    val cellIndex = cellIndex(index)
    val cell = data[cellIndex]
    val magic = (index - cellIndex * valuesPerLong) * bits

    return (cell shr magic and mask).toInt()
  }

  override fun getAndSet(index: Int, value: Int): Int {
    require(index in 0 until size) { "Index $index is out of bounds" }
    require(value in 0..mask) { "Value $value is out of bounds" }

    val cellIndex = cellIndex(index)
    val cell = data[cellIndex]
    val magic = (index - cellIndex * valuesPerLong) * bits
    val result = (cell shr magic and mask).toInt()

    data[cellIndex] = cell and (mask shl magic).inv() or (value.toLong() and mask shl magic)

    return result
  }

  override fun forEachIndexed(f: (index: Int, value: Int) -> Unit) {
    var i = 0
    val along = data
    val j = along.size

    for (k in 0 until j) {
      var l = along[k]
      repeat((0 until valuesPerLong).count()) {
        f(i, (l and mask).toInt())
      }

      ++i
      l = l shr bits
      if (i >= size) return
    }
  }

  override fun unpack(out: IntArray) {
    val dataSize = data.size
    var total = 0

    for (i in 0 until dataSize - 1) {
      var datum = data[i]
      for (j in 0 until valuesPerLong) {
        out[total + j] = (datum and mask).toInt()
        datum = datum shr bits
      }

      total += valuesPerLong
    }

    val remaining = size - total
    if (remaining > 0) {
      var datum = data[dataSize - 1]
      for (j in 0 until remaining) {
        out[total + j] = (datum and mask).toInt()
        datum = datum shr bits
      }
    }
  }

  override fun iterator(): IntIterator {
    var index = 0
    return object : IntIterator() {
      override fun hasNext(): Boolean = index < size

      override fun nextInt(): Int {
        index++
        return get(index - 1)
      }
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BitStorageImpl) return false

    if (bits != other.bits) return false
    if (size != other.size) return false
    if (!data.contentEquals(other.data)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = bits
    result = 31 * result + size
    result = 31 * result + data.contentHashCode()
    return result
  }

  private fun cellIndex(index: Int): Int =
    ((index.toLong() * divideMultiply + divideAdd) shr 32 shr divideShift).toInt()
}

private val MAGIC = intArrayOf(
  -1, -1, 0, Int.MIN_VALUE, 0, 0, 0x55555555, 0x55555555, 0, Int.MIN_VALUE, 0, 1, 0x33333333,
  0x33333333, 0, 0x2AAAAAAA, 0x2AAAAAAA, 0, 0x24924924, 0x24924924, 0, Int.MIN_VALUE, 0, 2,
  0x1C71C71C, 0x1C71C71C, 0, 0x19999999, 0x19999999, 0, 390451572, 390451572, 0, 0x15555555,
  0x15555555, 0, 0x13B13B13, 0x13B13B13, 0, 306783378, 306783378, 0, 0x11111111, 0x11111111, 0,
  Int.MIN_VALUE, 0, 3, 0xF0F0F0F, 0xF0F0F0F, 0, 0xE38E38E, 0xE38E38E, 0, 226050910, 226050910, 0,
  0xCCCCCCC, 0xCCCCCCC, 0, 0xC30C30C, 0xC30C30C, 0, 195225786, 195225786, 0, 186737708, 186737708,
  0, 0xAAAAAAA, 0xAAAAAAA, 0, 171798691, 171798691, 0, 0x9D89D89, 0x9D89D89, 0, 159072862,
  159072862, 0, 0x9249249, 0x9249249, 0, 148102320, 148102320, 0, 0x8888888, 0x8888888, 0,
  138547332, 138547332, 0, Int.MIN_VALUE, 0, 4, 130150524, 130150524, 0, 0x7878787, 0x7878787,
  0, 0x7507507, 0x7507507, 0, 0x71C71C7, 0x71C71C7, 0, 116080197, 116080197, 0, 113025455,
  113025455, 0, 0x6906906, 0x6906906, 0, 0x6666666, 0x6666666, 0, 104755299, 104755299, 0,
  0x6186186, 0x6186186, 0, 99882960, 99882960, 0, 97612893, 97612893, 0, 0x5B05B05, 0x5B05B05, 0,
  93368854, 93368854, 0, 91382282, 91382282, 0, 0x5555555, 0x5555555, 0, 87652393, 87652393, 0,
  85899345, 85899345, 0, 0x5050505, 0x5050505, 0, 0x4EC4EC4, 0x4EC4EC4, 0, 81037118, 81037118, 0,
  79536431, 79536431, 0, 78090314, 78090314, 0, 0x4924924, 0x4924924, 0, 75350303, 75350303, 0,
  74051160, 74051160, 0, 72796055, 72796055, 0, 0x4444444, 0x4444444, 0, 70409299, 70409299, 0,
  69273666, 69273666, 0, 0x4104104, 0x4104104, 0, Int.MIN_VALUE, 0, 5
)
