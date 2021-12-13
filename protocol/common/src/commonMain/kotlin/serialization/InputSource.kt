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

package com.gabrielleeg1.andesite.api.protocol.serialization

import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.readBytes
import okio.Buffer
import okio.Source
import okio.Timeout

internal class InputSource(val input: ByteReadPacket) : Source {
  override fun read(sink: Buffer, byteCount: Long): Long {
    sink.write(input.readBytes(sink.size.toInt()))

    return byteCount
  }
  
  override fun close(): Unit = input.close()

  override fun timeout(): Timeout = Timeout.NONE
}
