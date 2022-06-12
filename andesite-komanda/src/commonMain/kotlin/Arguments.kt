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

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

public class Argument<A : Any>(public val name: String, public val type: KClass<A>) {
  override fun toString(): String = "Argument<${type.simpleName}>(name=$name)"
}

public class ArgumentListBuilder {
  private var arguments: MutableSet<Argument<*>> = mutableSetOf()

  public fun <A : Any> add(argument: Argument<A>) {
    arguments += argument
  }

  public fun <A : Any> creating(type: KClass<A>): ArgumentBuilder<A> {
    return ArgumentBuilder(type, this)
  }

  public inline fun <reified A : Any> creating(): ArgumentBuilder<A> {
    return creating(A::class)
  }
}

public typealias ArgumentProvider<A> = PropertyDelegateProvider<Nothing?, ArgumentBuilder<A>>
public typealias ArgumentExecutes<A> = suspend ExecutionScope<*>.(value: String) -> A

public class ArgumentBuilder<A : Any>(
  private val type: KClass<A>,
  private val builder: ArgumentListBuilder,
) {
  private var name: String? = null
  private var executes: (suspend ExecutionScope<*>.(value: String) -> A)? = null
  private var suggests: ((text: String) -> Set<Suggestion>)? = null

  public fun executes(executes: ArgumentExecutes<A>): ArgumentBuilder<A> {
    this.executes = executes
    return this
  }

  public fun exactSuggests(suggests: (text: String) -> Set<Suggestion>): ArgumentBuilder<A> {
    this.suggests = suggests
    return this
  }

  public fun suggests(fn: MutableSet<Suggestion>.(String) -> Unit): ArgumentBuilder<A> {
    return exactSuggests { text ->
      buildSet {
        fn(text)
      }
    }
  }

  public fun build(): Argument<A> {
    return Argument(name!!, type)
  }

  public operator fun provideDelegate(
    _thisRef: Any?,
    property: KProperty<*>
  ): ReadOnlyProperty<Any?, Argument<A>> {
    name = property.name
    val argument = build().also { builder.add(it) }

    return ReadOnlyProperty { _, _ ->
      argument
    }
  }
}
