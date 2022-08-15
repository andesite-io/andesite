---
sidebar_position: 1
---

# Writing your first empty server

To write your first Java Edition empty server you will need to write a basic TCP server.

:::caution

The project only supports the Java Edition currently.

:::

## Dependencies

These are the dependencies required to run the server.

```
api("me.gabrielleeg1:andesite-protocol-common:$andesite_version")
api("me.gabrielleeg1:andesite-protocol-java:$andesite_version")

implementation("io.ktor:ktor-network:$ktor_version")

implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_json_version")

implementation("net.benwoodworth.knbt:knbt:$knbt_version")
```

## Getting Started

You will need to create a simple TCP server with Ktor.

```kotlin title="main.kt"
suspend fun main(): Unit = coroutineScope {
  val server = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp().bind("0.0.0.0", 25565)

  println("Listening connections at ${server.localAddress} for protocol ${codec.configuration.protocolVersion}")
  
  while(true) {
    val client = server.accept()

    launch(CoroutineName("session-${client.remoteAddress}")) {
      // TODO: handle the connection
    }
  }
}
```

And after that, you will instantiate the MinecraftCodec:

```kotlin title="main.kt"
val codec = MinecraftCodec {
  protocolVersion = 756 // 756 is the sample version for the project, for 1.17.1;
  nbt = Nbt { // Here you can configure the Nbt settings;
    variant = NbtVariant.Java
    compression = NbtCompression.None
    ignoreUnknownKeys = true
  }
  json = Json { // and here, the json ones;
    prettyPrint = true
  }
  serializersModule = SerializersModule { // here you can add new contextual serializers, etc...
    contextual(UuidSerializer)
  }
}
```

You will need a Session class to read and send packets, here is the snippet:

```kotlin title="Session.kt"
@OptIn(ExperimentalSerializationApi::class)
class Session(val codec: MinecraftCodec, val socket: Socket, val scope: CoroutineScope) : CoroutineScope by scope {
  val input = socket.openReadChannel()
  val output = socket.openWriteChannel()

  suspend fun <T : JavaPacket> receivePacket(deserializer: DeserializationStrategy<T>): T {
    val name = deserializer.descriptor.serialName

    val size = input.readVarInt()
    val packet = input.readPacket(size.toInt())

    val id = packet.readVarInt().toInt()

    println("Packet `$name` received with id [0x%02x] and size [$size]".format(id))

    return codec.decodeFromByteArray(deserializer, packet.readBytes())
  }

  suspend fun <T : JavaPacket> sendPacket(serializer: SerializationStrategy<T>, packet: T) {
    val packetName = serializer.descriptor.serialName
    val packetId = extractPacketId(serializer.descriptor)

    output.writePacket {
      val data = buildPacket {
        writeVarInt(packetId)
        writeFully(codec.encodeToByteArray(serializer, packet))
      }

      println("Packet `$packetName` sent with id [0x%02x] with size [${data.remaining}]".format(packetId))

      writeVarInt(data.remaining.toInt())
      writePacket(data)
    }

    output.flush()
  }
}

```

:::caution

This class is not production-ready. It is only a snippet to bootstrap the empty server.

:::

Now with the `Session` class, you can handle the connections:

```kt title="main.kt"
val session = Session(codec, client, this)
val handshake = session.receivePacket(HandshakePacket.serializer())

when (handshake.nextState) {
  NextState.Status -> {
    session.sendPacket(
      ResponsePacket.serializer(),
      ResponsePacket(
        Response(
          version = Version(name = "Andesite for 1.17.1", protocol = 756),
          players = Players(max = 10, online = 0, sample = listOf()),
          description = Chat.of("&eAndesite for 1.17.1"), // The Chat API is a wrapper for Minecraft text components
        ),
      ),
    )

    session.receivePacket(PingPacket.serializer())
    session.sendPacket(PongPacket.serializer(), PongPacket())
  }

  NextState.Login -> TODO("implement login logic")
}
```

## The final source

The source code built with this tutorial is [here](https://gist.github.com/33b13abb79ff051f669fbcf40d01878b)
