## andesite-protocol

### Installation

You can use the gradle groovy with the following code:

```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation 'me.gabrielleeg1:andesite-protocol-java-v756:{latest_version}' // Example for version v756 protocol
  implementation 'me.gabrielleeg1:andesite-protocol-java-v756-jvm:{latest_version}'
}
```

Or either with kotlin gradle dsl:

```kt
repositories {
  mavenCentral()
}

dependencies {
  implementation("me.gabrielleeg1:andesite-protocol-java-v756:{latest_version}") // Example for version v756 protocol
  implementation("me.gabrielleeg1:andesite-protocol-java-v756-jvm:{latest_version}")
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
