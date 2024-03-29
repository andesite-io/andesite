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

package andesite.protocol

import java.io.File

public fun resource(path: String): File {
  return ClassLoader.getSystemResource(path)
    ?.file
    ?.let(::File)
    ?: error("Can not find resource $path")
}

public fun readResource(path: String): ByteArray {
  return ClassLoader.getSystemResource(path)
    ?.readBytes()
    ?: error("Can not find resource $path")
}
