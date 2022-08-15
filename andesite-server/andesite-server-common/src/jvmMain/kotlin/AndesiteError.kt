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

package andesite

/**
 * Represents an Andesite business error.
 *
 * @param message the error message
 * @param cause the cause of the error
 */
public class AndesiteError(override val message: String, cause: Throwable? = null) :
  Exception(message, cause)

/**
 * Creates a new [AndesiteError] formatting the given [message] and [args].
 *
 * @param message the error message
 * @param args the arguments to format the message
 * @return a new [AndesiteError]
 */
public fun andesiteError(message: String, vararg args: Any?): Nothing {
  throw AndesiteError(message.format(args = args))
}
