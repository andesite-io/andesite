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

package andesite.komanda

import kotlin.reflect.KClass

public typealias Parameters = Map<String, Parameter<*>>

public class ParametersBuilder {
  private var parameters: MutableSet<Parameter<*>> = mutableSetOf()

  public fun <A> add(parameter: Parameter<A>) {
    parameters += parameter
  }

  public fun <A : Any> creating(type: KClass<A>): ParameterBuilder<A> {
    return ParameterBuilder(type, this)
  }

  public inline fun <reified A : Any> creating(): ParameterBuilder<A> {
    return creating(A::class)
  }

  public fun byte(): ParameterBuilder<Byte> = creating<Byte>().executes { it.toByte() }

  public fun uByte(): ParameterBuilder<UByte> = creating<UByte>().executes { it.toUByte() }

  public fun short(): ParameterBuilder<Short> = creating<Short>().executes { it.toShort() }

  public fun uShort(): ParameterBuilder<UShort> = creating<UShort>().executes { it.toUShort() }

  public fun int(): ParameterBuilder<Int> = creating<Int>().executes { it.toInt() }

  public fun uInt(): ParameterBuilder<UInt> = creating<UInt>().executes { it.toUInt() }

  public fun long(): ParameterBuilder<Long> = creating<Long>().executes { it.toLong() }

  public fun uLong(): ParameterBuilder<ULong> = creating<ULong>().executes { it.toULong() }

  public fun string(): ParameterBuilder<String> = creating<String>().executes { it }

  public fun build(): Set<Parameter<*>> {
    return parameters.toSet()
  }
}
