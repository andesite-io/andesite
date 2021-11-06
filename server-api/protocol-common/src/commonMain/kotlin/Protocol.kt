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

package com.gabrielleeg1.javarock.api.protocol

import kotlin.reflect.KClass

class Protocol(val codecs: Map<KClass<*>, Codec<*>>) {
  // TODO
}

fun protocol(builder: ProtocolBuilder.() -> Unit): Protocol {
  return ProtocolBuilder().apply(builder).build()
}

class ProtocolBuilder internal constructor() {
  private val builder = LinkedHashMap<KClass<*>, Codec<*>>()

  inline fun <reified T : Any> codec(codec: Codec<T>) {
    codec(T::class, codec)
  }

  fun <T : Any> codec(aClass: KClass<T>, codec: Codec<T>) {
    builder[aClass] = codec
  }

  internal fun build(): Protocol {
    return Protocol(builder)
  }
}
