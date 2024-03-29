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

/**
 * Public API marked with this annotation is effectively internal, which means it should not be used
 * outside of Andesite. Signature, semantics, source and binary compatibilities are not guaranteed
 * for this API and will be changed without any warnings or migration aids. If you cannot avoid
 * using internal API to solve your problem, please report your use-case to the issue tracker.
 */
@RequiresOptIn(
  level = RequiresOptIn.Level.ERROR,
  message = "This API is internal in andesite and should not be used." +
    " It could be removed or changed without notice.",
)
@Retention(AnnotationRetention.BINARY)
public annotation class AndesiteInternalAPI
