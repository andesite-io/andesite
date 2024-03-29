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

@file:OptIn(ExperimentalTime::class)

package andesite.java.player

import andesite.protocol.java.JavaPacket
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.serializer
import kotlin.time.ExperimentalTime

internal suspend inline fun <reified T : JavaPacket> Session.awaitPacket(): T? {
  return inboundPacketFlow.filterIsInstance<T>().firstOrNull()
}

internal suspend inline fun <reified T : JavaPacket> Session.receivePacket(): T {
  return receivePacket(serializer())
}

internal suspend inline fun <reified T : JavaPacket> Session.sendPacket(packet: T) {
  sendPacket(serializer(), packet)
}
