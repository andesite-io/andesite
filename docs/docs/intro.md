---
sidebar_position: 1
---

# Introduction

Andesite is a simple Minecraft protocol library. It is designed to develop a Minecraft server software.

## Getting Started

Setup your gradle project:

```kotlin title="build.gradle.kts"
repositories {
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") 
}

dependencies {
  api("me.gabrielleeg1:andesite-protocol-common:$andesite_version")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_json_version")

  implementation("net.benwoodworth.knbt:knbt:$knbt_version")
}
```

### What you'll need

- [Kotlin](https://kotlinlang.org/) version 1.7.10 or above.
- [Gradle](https://gradle.org/) project.
