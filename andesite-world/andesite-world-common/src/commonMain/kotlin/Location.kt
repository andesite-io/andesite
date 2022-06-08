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

package andesite.world

/**
 * Class that represents a location in the game.
 */
public data class Location(
  val x: Double,
  val y: Double,
  val z: Double,
  val yaw: Float,
  val pitch: Float,
  val world: World,
) {
  public operator fun div(other: Location): Location {
    return Location(
      x / other.x,
      y / other.y,
      z / other.z,
      yaw / other.yaw,
      pitch / other.pitch,
      world,
    )
  }

  public operator fun times(other: Location): Location {
    return Location(
      x * other.x,
      y * other.y,
      z * other.z,
      yaw * other.yaw,
      pitch * other.pitch,
      world,
    )
  }

  public operator fun minus(other: Location): Location {
    return Location(
      x - other.x,
      y - other.y,
      z - other.z,
      yaw - other.yaw,
      pitch - other.pitch,
      world,
    )
  }

  public operator fun plus(other: Location): Location {
    return Location(
      x + other.x,
      y + other.y,
      z + other.z,
      yaw + other.yaw,
      pitch + other.pitch,
      world,
    )
  }
}
