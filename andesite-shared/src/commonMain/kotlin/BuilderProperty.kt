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

package andesite.shared

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** Represents a property delegate to be used in builder classes. */
public class BuilderProperty<T : Any> : ReadWriteProperty<Any?, T> {
  private var value: T? = null

  override fun getValue(thisRef: Any?, property: KProperty<*>): T {
    return value ?: throw BuilderInitializationException(
      message = "Property ${property.name} should be initialized before build.",
    )
  }

  override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    this.value = value
  }
}

public class BuilderInitializationException(override val message: String) : RuntimeException()

public fun <R, A> R.runIfInitialized(property: () -> A, f: R.(A) -> Unit) {
  try {
    f(property())
  } catch (_: BuilderInitializationException) {
    // nothing
  }
}
