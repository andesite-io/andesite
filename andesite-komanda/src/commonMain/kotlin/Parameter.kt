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

import andesite.shared.AndesiteProperties
import andesite.shared.runIfInitialized
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

public typealias ArgumentProvider<A> = PropertyDelegateProvider<Nothing?, ParameterBuilder<A>>
public typealias ArgumentExecutes<A> = suspend (value: String) -> A
public typealias ArgumentSuggests = suspend (text: String) -> Set<Suggestion>
public typealias ArgumentSuggestsBuilder = suspend MutableSet<Suggestion>.(text: String) -> Unit

public class Parameter<A>(
  public val name: String,
  public val type: KClass<A & Any>,
  public val executes: ArgumentExecutes<A>,
  public val suggests: ArgumentSuggests,
  public val nullable: Boolean,
) {
  override fun toString(): String = "Parameter<${type.simpleName}>(name=$name)"

  internal var localScope: ExecutionScope<*>? by AndesiteProperties.threadLocal()

  public operator fun getValue(thisRef: Nothing?, property: KProperty<*>): A {
    val arguments = localScope?.arguments
      ?: error("Could not find execution scope in current thread")

    return if (nullable) {
      // Suppress unchecked cast, if the nullable is true, the A will be nullable
      @Suppress("UNCHECKED_CAST")
      arguments.getOrNull(name, type) as A
    } else {
      arguments.get(name, type)
    }
  }
}

public class ParameterBuilder<A>(
  private val type: KClass<A & Any>,
  private val builder: ParametersBuilder,
  private val nullable: Boolean = false,
) {
  private var name: String by AndesiteProperties.builder()
  private var executes: ArgumentExecutes<A> by AndesiteProperties.builder()
  private var suggests: ArgumentSuggests = { setOf() }

  @Suppress("UNCHECKED_CAST")
  public fun nullable(): ParameterBuilder<A?> =
    ParameterBuilder(type, builder, nullable = true).apply {
      runIfInitialized(this@ParameterBuilder::name) { this@runIfInitialized.name = it }
      runIfInitialized(this@ParameterBuilder::suggests) { this@runIfInitialized.suggests = it }
      runIfInitialized(this@ParameterBuilder::executes) {
        this@runIfInitialized.executes = it as ArgumentExecutes<A & Any>
      }
    } as ParameterBuilder<A?>

  public fun executes(executes: ArgumentExecutes<A>): ParameterBuilder<A> {
    this.executes = executes
    return this
  }

  public fun exactSuggests(suggests: ArgumentSuggests): ParameterBuilder<A> {
    this.suggests = suggests
    return this
  }

  public fun suggests(fn: ArgumentSuggestsBuilder): ParameterBuilder<A> {
    return exactSuggests { text ->
      buildSet {
        fn(text)
      }
    }
  }

  internal fun build(): Parameter<A> {
    return Parameter(name, type, executes, suggests, nullable)
  }

  public operator fun provideDelegate(
    _thisRef: Any?,
    property: KProperty<*>,
  ): Parameter<A> {
    name = property.name

    return build().also { parameter -> builder.add(parameter) }
  }
}
