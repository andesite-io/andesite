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

package andesite.protocol.serialization

import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.DoubleArraySerializer
import kotlinx.serialization.builtins.FloatArraySerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.serializer

public fun ProtocolEncoder.encodeLongArray(array: LongArray) {
  encodeSerializableValue(LongArraySerializer(), array)
}

public fun ProtocolEncoder.encodeIntArray(array: IntArray) {
  encodeSerializableValue(IntArraySerializer(), array)
}

public fun ProtocolEncoder.encodeFloatArray(array: FloatArray) {
  encodeSerializableValue(FloatArraySerializer(), array)
}

public fun ProtocolEncoder.encodeDoubleArray(array: DoubleArray) {
  encodeSerializableValue(DoubleArraySerializer(), array)
}

public fun ProtocolEncoder.encodeByteArray(array: ByteArray) {
  encodeSerializableValue(ByteArraySerializer(), array)
}

public inline fun <reified T : Any> ProtocolEncoder.encodeList(list: List<T>) {
  encodeSerializableValue(ListSerializer(serializer()), list)
}
