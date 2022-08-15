---
sidebar_position: 1
---

# Listening events

Listening events on Andesite server is an easy task with [Kotlin Flow](https://kotlinlang.org/docs/flow.html).

## Getting Started

You can listen events from the server with the `on()` function, and it passes the event as a receiver:

```kotlin title="main.kt"
val server: MinecraftServer = ...

server.on<PlayerJoinEvent> { // this: PlayerJoinEvent ->
  player.sendMessage("&aWelcome to the server!") // The `sendMessage` function uses Chat too, so you can use colors.
}
```

### Event holders

Like you can listen events from the server with the `on()` function, you can listen events from a Player, and anything that implements [EventHolder](https://github.com/gabrielleeg1/andesite/blob/main/andesite-server/andesite-server-common/src/jvmMain/kotlin/event/EventHolder.kt):

```kotlin title="main.kt"

val player: Player = ...

player.on<PlayerMoveEvent> {
  // Scoped to the player listening
}

```

:::tip
You can use this to filter events to a specific player.
:::
