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

import andesite.komanda.parsing.parseCommandString

public interface KomandaRoot {
  public val komanda: KomandaSettings

  public fun komanda(configure: KomandaSettingsBuilder.() -> Unit)

  public fun command(name: String, builder: CommandBuilder.() -> Unit)

  public suspend fun <S : Any> dispatch(string: String, sender: S)
}

public fun KomandaRoot(configure: KomandaSettingsBuilder.() -> Unit): KomandaRoot {
  return KomandaRootImpl(KomandaSettings(configure))
}

private class KomandaRootImpl(override var komanda: KomandaSettings) : KomandaRoot {
  val commands: MutableMap<String, Command> = mutableMapOf()

  override fun komanda(configure: KomandaSettingsBuilder.() -> Unit) {
    komanda = KomandaSettings(configure)
  }

  override fun command(name: String, builder: CommandBuilder.() -> Unit) {
    commands[name] = CommandBuilderImpl(name).apply(builder).build()
  }

  override suspend fun <S : Any> dispatch(string: String, sender: S) {
    println(parseCommandString(string))
  }
}
