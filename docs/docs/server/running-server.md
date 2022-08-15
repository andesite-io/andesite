---
sidebar_position: 1
---

# Running the builtin server

The builtin is a Minecraft lightweight server implementation.

:::caution

The project only supports the Java Edition 1.17.1 currently.

:::

### Dependencies

These are the dependencies required to run the server.

```
api("me.gabrielleeg1:andesite-komanda:1.0.3-SNAPSHOT")

api("me.gabrielleeg1:andesite-protocol-common:1.0.3-SNAPSHOT")
api("me.gabrielleeg1:andesite-protocol-java-v756:1.0.3-SNAPSHOT")

api("me.gabrielleeg1:andesite-world-common:1.0.3-SNAPSHOT")
api("me.gabrielleeg1:andesite-world-anvil:1.0.3-SNAPSHOT")

api("me.gabrielleeg1:andesite-server-common:1.0.3-SNAPSHOT")
api("me.gabrielleeg1:andesite-server-java:1.0.3-SNAPSHOT")

implementation("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")
implementation("org.apache.logging.log4j:log4j-api:2.17.2")
implementation("org.apache.logging.log4j:log4j-core:2.18.0")

implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0-RC")

implementation("net.benwoodworth.knbt:knbt:0.11.2")
```

### Getting Started

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
