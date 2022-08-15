---
sidebar_position: 2
---

# Implementing a Packet

To implement a custom packet you will need to use the annotations like in the [Join Game Packet](https://github.com/gabrielleeg1/andesite/blob/main/andesite-protocol/andesite-protocol-java/andesite-protocol-java-v756/src/commonMain/kotlin/JoinGamePacket.kt) for [protocol 756](https://wiki.vg/index.php?title=Protocol&oldid=16918):

```kt title="JoinGamePacket.kt"
@ProtocolPacket(0x26) // The @ProtocolPacket annotations specifies the packet id
@SerialName("JoinGamePacket") 
@Serializable // The @Serializable is the kotlinx.serialization annotation to enable serialization for this class
public data class JoinGamePacket(
  val entityId: Int,
  val isHardcore: Boolean,
  val gameMode: GameMode, // You can pass enums with @ProtocolEnum annotation to be serialized with the packet
  val previousGameMode: PreviousGameMode,
  val worlds: List<Identifier>,
  @ProtocolNbt val dimensionCodec: DimensionCodec, // You can use the @ProtocolNbt annotation to specify the Nbt codec
  @ProtocolNbt val dimension: Dimension,
  val world: Identifier,
  val hashedSeed: Long,
  val maxPlayers: VarInt,
  val viewDistance: VarInt,
  val reducedDebugInfo: Boolean,
  val enableRespawnScreen: Boolean,
  val isDebug: Boolean,
  val isFlat: Boolean,
) : JavaPacket

@Serializable
@SerialName("PreviousGameMode")
@ProtocolEnum // The @ProtocolEnum annotation is used to enable the enum serialization
@ProtocolVariant(Variant.Byte) // The @ProtocolVariant specifies the type of the enum
public enum class PreviousGameMode {
  @ProtocolValue(-1) Unknown, // The @ProtocolValue annotation specifies the value of the enum
  @ProtocolValue(0) Survival,
  @ProtocolValue(1) Creative,
  @ProtocolValue(2) Adventure,
  @ProtocolValue(3) Spectator;

  public fun toGameMode(): GameMode? {
    return when (this) {
      Unknown -> null
      else -> GameMode.values()[ordinal]
    }
  }
}

@Serializable
@SerialName("GameMode")
@ProtocolEnum
@ProtocolVariant(Variant.UByte)
public enum class GameMode {
  @ProtocolValue(0) Survival,
  @ProtocolValue(1) Creative,
  @ProtocolValue(2) Adventure,
  @ProtocolValue(3) Spectator;
}
```

:::tip
You can use @ProtocolJson to specify the json codec for the field as the same you can use @ProtocolNbt.
:::

You will also need to add it to the `PacketRegistry` in the codec:

```kotlin title="codec.kt"
val codec = MinecraftCodec {
  protocolVersion = 756
  packetRegistry = createPacketRegistry {
    register<JoinGamePacket>()
    // ...other packets
  }
  // ...other configs
}
```
