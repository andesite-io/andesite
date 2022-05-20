# andesite

![GitHub stars](https://img.shields.io/github/stars/gabrielleeg1/andesite?color=purple&style=for-the-badge)
![GitHub issues](https://img.shields.io/github/issues/gabrielleeg1/andesite?color=purple&style=for-the-badge)
![GitHub last commit](https://img.shields.io/github/last-commit/gabrielleeg1/andesite?color=purple&style=for-the-badge)
![GitHub latest version](https://img.shields.io/github/v/release/gabrielleeg1/andesite?color=purple&style=for-the-badge)

Simple library for building [Minecraft Protocol](https://wiki.vg/Main_Page) for Minecraft Java Edition and Bedrock
Edition.

### Implemented versions
The following versions of Minecraft are supported:

- [Java](protocol/java)
  - [v756](protocol/java/v756)

### Installation
You can use the gradle groovy with the following code:

```groovy
repositories {
  maven { url 'todo' }
}

dependencies {
  implementation 'com.gabrielleeg1.andesite:protocol:common:{latest_version}'
  implementation 'com.gabrielleeg1.andesite:protocol:java:v756:{latest_version}' // Example for version v756 protocol
}
```

Or either with kotlin gradle dsl:

```kt
repositories {
  maven("todo")
}

dependencies {
  implementation("com.gabrielleeg1.andesite:protocol:common:{latest_version}")
  implementation("com.gabrielleeg1.andesite:protocol:java:v756:{latest_version}") // Example for version v756 protocol
}
```

### Samples
Samples to use with andesite

#### Java protocol
This is a simple example of using `andesite:protocol`:

- PS: The example is for [v756 protocol](https://wiki.vg/index.php?title=Protocol&oldid=16918)

```kt
@ProtocolPacket(0x00)
@Serializable
data class HandshakePacket(
  val protocolVersion: VarInt,
  val serverAddress: String,
  val serverPort: UShort,
  val nextState: NextState,
)

@ProtocolEnum
@ProtocolVariant(Variant.VarInt)
@Serializable
enum class NextState {
  @ProtocolValue(1)
  Status,

  @ProtocolValue(2)
  Login;
}

val codec = MinecraftCodec.v756 { json = Json; nbt = SerializersModule { contextual(UuidSerializer) } }
val packet = codec.decodeFromByteArray<HandshakePacket>(bytes)

println(packet)
```

#### Anvil world
This is a simple example of loading an anvil world with `andesite:world:anvil`:

- PS: The example is for [v756 protocol](https://wiki.vg/index.php?title=Protocol&oldid=16918)
- PS: This only works in `jvm` target.

```kt
fun resource(path: String): File {
  return ClassLoader.getSystemResource(path)
    ?.file
    ?.let(::File)
    ?: error("Can not find resource $path")
}

val blockRegistry = readBlockRegistry(resource("v756").resolve("blocks.json").readText())
val anvilWorld: AnvilWorld = readAnvilWorld(blockRegistry, resource("world"))

println(anvilWorld)
```

### Licensing
This project is using the [Apache 2 License](LICENSE).

```
Copyright 2022 Gabrielle Guimarães de Oliveira

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
