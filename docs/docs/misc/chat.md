---
sidebar_position: 1
---

# Chat

The [Chat](https://github.com/gabrielleeg1/andesite/blob/main/andesite-protocol/andesite-protocol-common/src/commonMain/kotlin/misc/Chat.kt) is a wrapper for the Minecraft text components.

### Simple chat

You can instantiate a simple chat with the `Chat.of()` function:

```kotlin title="main.kt"
Chat.of("&aI &bcan &cuse &dcolors!")
```

:::tip
It is supported all bukkit color codes with the `&` prefix. You can look [here](https://wiki.vg/Chat#Colors) for the full list of color codes.
:::


### Placeholder chat

You can use chat with placeholders to make the chat more customized like:

```kotlin title="main.kt"
Chat.build("{player} joined the server") {
  val player by placeholder(player.username) {
    hoverEvent = ShowText("@${player.username}")

    hex("32a852") // you can use `yellow()` or `hex()` to set the color of the text.
  }
}
```

:::tip
You can set up click events too, the complete list of events are:

- Hover
  * Show text

- Click
  * Open url
  * Run command
  * Suggest command
  * Change page (of book)
  * Copy to clipboard
:::

### Mordant

You can transform the `Chat` into text to print into a colored terminal using `.mordant()` function like:

```kotlin title="main.kt"
println(Chat.of("&aI &bcan &cuse &dcolors!").mordant())
```
