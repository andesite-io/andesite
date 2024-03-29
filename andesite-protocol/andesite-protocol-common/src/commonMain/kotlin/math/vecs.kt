/*
 *    Copyright 2021 Gabrielle Guimarães de Oliveira
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

package andesite.protocol.math

/**
 * Vector used for 3D math using integer coordinates.
 *
 * @property x X coordinate
 * @property y Y coordinate
 * @property z Z coordinate
 */
public data class Vector3i(val x: Int, val y: Int, val z: Int)

/**
 * Vector used for 2D math using integer coordinates.
 *
 * @property x X coordinate
 * @property y Y coordinate
 */
public data class Vector2i(val x: Int, val y: Int)

/**
 * Vector used for 3D math using float coordinates.
 *
 * @property x X coordinate
 * @property y Y coordinate
 * @property z Z coordinate
 */
public data class Vector3f(val x: Float, val y: Float, val z: Float)

/**
 * Vector used for 2D math using float coordinates.
 *
 * @property x X coordinate
 * @property y Y coordinate
 */
public data class Vector2f(val x: Float, val y: Float)
