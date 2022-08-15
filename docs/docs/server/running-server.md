---
sidebar_position: 1
---

# Running the builtin server

The builtin is a Minecraft lightweight server implementation.

:::caution

The project only supports the Java Edition 1.17.1 currently.

:::

## Dependencies

These are the dependencies required to run the server.

```
api("me.gabrielleeg1:andesite-komanda:$andesite_version")

api("me.gabrielleeg1:andesite-protocol-common:$andesite_version")
api("me.gabrielleeg1:andesite-protocol-java-v756:$andesite_version")

api("me.gabrielleeg1:andesite-world-common:$andesite_version")
api("me.gabrielleeg1:andesite-world-anvil:$andesite_version")

api("me.gabrielleeg1:andesite-server-common:$andesite_version")
api("me.gabrielleeg1:andesite-server-java:$andesite_version")

implementation("org.apache.logging.log4j:log4j-api-kotlin:$log4j2_kotlin_version")
implementation("org.apache.logging.log4j:log4j-api:$log4j2_version")
implementation("org.apache.logging.log4j:log4j-core:$log4j2_version")

implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_json_version")

implementation("net.benwoodworth.knbt:knbt:$knbt_version")
```

## Getting Started

Here, you will only need to consume the existing API:

```kotlin title="main.kt"
val server = createJavaServer(SupervisorJob()) {
  blockRegistry = ClassLoader
    .getSystemResource("v756/blocks.json")
    ?.readText()
    ?.let(::readBlockRegistry)
    ?: error("Can not find block registry for v756 java protocol")

  codec = MinecraftCodec.v756 {
    nbt = Nbt {
      variant = NbtVariant.Java
      compression = NbtCompression.None
      ignoreUnknownKeys = true
    }

    json = Json {
      prettyPrint = true
    }

    serializersModule = SerializersModule {
      contextual(UuidSerializer)
    }
  }

  hostname = "127.0.0.1"
  port = 25565
  spawn = Location(
    0.0,
    10.0,
    0.0,
    0f,
    0f,
    readAnvilWorld(blockRegistry, resource("world")),
  )

  motd {
    maxPlayers = 20
    version = "Andesite for 1.17.1"
    text = Chat.of("&7A Minecraft Server")
  }
}

server.listen()
```

Here you go! You have a void server running.
