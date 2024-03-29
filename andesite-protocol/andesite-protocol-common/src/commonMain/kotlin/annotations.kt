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

@file:OptIn(ExperimentalSerializationApi::class)

package andesite.protocol

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.Serializable

/**
 * Determines packet id.
 *
 * @param id packet id.
 */
@SerialInfo
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class ProtocolPacket(val id: Int)

/**
 * Sets packet's field to decode with Json format.
 */
@SerialInfo
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.TYPE)
public annotation class ProtocolJson

/**
 * Sets packet's field to decode with Nbt format.
 */
@SerialInfo
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.TYPE)
public annotation class ProtocolNbt

/**
 * Sets packet's field to decode string with [max] size.
 *
 * @param max maximum size of string.
 */
@SerialInfo
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.TYPE)
public annotation class ProtocolString(val max: Int)

/**
 * Determines a number enum.
 */
@SerialInfo
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class ProtocolEnum

/**
 * Determines a number value.
 *
 * @param value number value.
 */
@SerialInfo
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
public annotation class ProtocolValue(val value: Int)

/**
 * Determines the int variant to the List or Enum.
 */
@SerialInfo
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
public annotation class ProtocolVariant(val kind: Variant)

@Serializable
public enum class Variant {
  VarInt, VarLong,
  UByte, Byte,
  UInt, Int,
}
