## andesite-world

### Installation

You can use the gradle groovy with the following code:

```groovy
repositories {
  mavenCentral()
}

dependencies {
  implementation 'me.gabrielleeg1:andesite-world-anvil:{latest_version}' // Example for version v756 protocol
}
```

Or either with kotlin gradle dsl:

```kt
repositories {
  mavenCentral()
}

dependencies {
  implementation("me.gabrielleeg1:andesite-world-anvil:{latest_version}") // Example for version v756 protocol
}
```

### Samples

Samples to use with andesite

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
