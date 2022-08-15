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

/** Andesite property delegates. */
public object AndesiteProperties {
  /**
   * Creates a new [NullableThreadLocalProperty].
   *
   * @return a new [NullableThreadLocalProperty]
   */
  public fun <A : Any> threadLocal(): NullableThreadLocalProperty<A> = NullableThreadLocalProperty()

  /**
   * Creates a new [ThreadLocalProperty] with the given [value].
   *
   * @param value the value to be set initially
   * @return a new [ThreadLocalProperty]
   */
  public fun <A : Any> threadLocal(value: A): ThreadLocalProperty<A> = ThreadLocalProperty(value)

  /**
   * Creates a new [BuilderProperty].
   *
   * @return a new [BuilderProperty]
   */
  public fun <A : Any> builder(): BuilderProperty<A> = BuilderProperty()
}
