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

public class Argument

public class ArgumentListBuilder {
  private var arguments: MutableSet<Argument> = mutableSetOf()

  public fun adding(argument: Argument) {
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

  public fun build(): Argument {
    return Argument()
  }

  public operator fun provideDelegate(
    _thisRef: Any?,
    property: KProperty<*>
  ): ReadOnlyProperty<Any?, A> {
    builder.adding(build())

    return ReadOnlyProperty { thisRef, _ ->
      when (thisRef) {
        is ExecutionScope<*> -> thisRef.arguments.get(property.name, type)
        else -> error("Could not get argument value of ${property.name} in scope $thisRef")
      }
    }
  }
}
