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

import andesite.komanda.errors.CommandFailure
import andesite.protocol.misc.Chat
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

public class KomandaSettings(
  public val typeAliases: Map<String, KClass<*>> = emptyMap(),
  public val acceptedTargets: Map<KClass<*>, Chat> = emptyMap(),
  public val exceptionHandlers: Set<ExceptionHandler> = emptySet(),
  public val adapters: Map<KClass<*>, Any> = emptyMap(),
)

public val DefaultKomandaSettings: KomandaSettings = KomandaSettings()

public fun KomandaSettings(
  from: KomandaSettings = DefaultKomandaSettings,
  configure: KomandaSettingsBuilder.() -> Unit,
): KomandaSettings {
  return KomandaSettingsBuilder(from).apply(configure).build()
}

public typealias ArgumentParser<A> = (text: String) -> A

public typealias ExceptionHandler = suspend ExecutionScope<Any>.(failure: CommandFailure) -> Unit

public typealias Execution<S> = suspend ExecutionScope<S>.() -> Unit

public class KomandaSettingsBuilder(private val from: KomandaSettings) {
  public val typeAliases: MutableMap<String, KClass<*>> = from.typeAliases.toMutableMap()
  public val acceptedTargets: MutableMap<KClass<*>, Chat> = from.acceptedTargets.toMutableMap()

  @PublishedApi
  internal val adapters: MutableMap<KClass<*>, Any> = mutableMapOf()

  @PublishedApi
  internal val exceptionHandlers: MutableSet<ExceptionHandler> = mutableSetOf()

  public fun <A : Any> argument(type: KClass<A>, parser: (String) -> A) {
    adapters[type] = parser
  }

  public fun onFailure(handler: ExceptionHandler) {
    exceptionHandlers += handler
  }

  public fun acceptTargets(builder: MutableMap<KClass<*>, Chat>.() -> Unit) {
    acceptedTargets.apply(builder)
  }

  public inline fun <reified A : Any> alias(name: String) {
    typeAliases[name] = A::class
  }

  public inline fun <reified A : Any> argument(noinline parser: (String) -> A) {
    val type = typeOf<A>()
    val klass = type as? KClass<*> ?: error("$type must be a class type")

    adapters[klass] = parser
  }

  public fun build(): KomandaSettings {
    return KomandaSettings(typeAliases, acceptedTargets, exceptionHandlers, adapters)
  }
}
