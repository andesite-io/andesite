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
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

public class Parameter<A : Any>(
  public val name: String,
  public val type: KClass<A>,
  public val executes: ArgumentExecutes<A>,
  public val suggests: ArgumentSuggests,
) {
  override fun toString(): String = "Argument<${type.simpleName}>(name=$name)"

  public var localScope: ExecutionScope<*>? by AndesiteProperties.threadLocal()

  public operator fun getValue(thisRef: Nothing?, property: KProperty<*>): A {
    return localScope
      ?.arguments
      ?.get(name, type)
      ?: error("Could not find execution scope in current thread")
  }
}

public typealias ArgumentProvider<A> = PropertyDelegateProvider<Nothing?, ArgumentBuilder<A>>
public typealias ArgumentExecutes<A> = suspend (value: String) -> A
public typealias ArgumentSuggestsBuilder = suspend MutableSet<Suggestion>.(text: String) -> Unit
public typealias ArgumentSuggests = suspend (text: String) -> Set<Suggestion>

public class ArgumentBuilder<A : Any>(
  private val type: KClass<A>,
  private val builder: ParametersBuilder,
) {
  private var name: String? = null
  private var executes: ArgumentExecutes<A>? = null
  private var suggests: ArgumentSuggests = { setOf() }

  public fun executes(executes: ArgumentExecutes<A>): ArgumentBuilder<A> {
    this.executes = executes
    return this
  }

  public fun exactSuggests(suggests: ArgumentSuggests): ArgumentBuilder<A> {
    this.suggests = suggests
    return this
  }

  public fun suggests(fn: ArgumentSuggestsBuilder): ArgumentBuilder<A> {
    return exactSuggests { text ->
      buildSet {
        fn(text)
      }
    }
  }

  public fun build(): Parameter<A> {
    return Parameter(name!!, type, executes!!, suggests)
  }

  public operator fun provideDelegate(
    _thisRef: Any?,
    property: KProperty<*>
  ): Parameter<A> {
    name = property.name

    val argument = build()
    builder.add(argument)
    return argument
  }
}

public class ParametersBuilder {
  private var parameters: MutableSet<Parameter<*>> = mutableSetOf()

  public fun <A : Any> add(parameter: Parameter<A>) {
    parameters += parameter
  }

  public fun <A : Any> creating(type: KClass<A>): ArgumentBuilder<A> {
    return ArgumentBuilder(type, this)
  }

  public inline fun <reified A : Any> creating(): ArgumentBuilder<A> {
    return creating(A::class)
  }

  public fun build(): Set<Parameter<*>> {
    return parameters.toSet()
  }
}
