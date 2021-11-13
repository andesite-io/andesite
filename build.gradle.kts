/*
 *    Copyright 2021 Gabrielle Guimarães de Oliveira
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

plugins {
  kotlin("multiplatform") version "1.5.31"
  kotlin("plugin.serialization") version "1.5.31"
}

group = "com.gabrielleeg1"
version = "1.0-SNAPSHOT"

allprojects {
  apply(plugin = "org.jetbrains.kotlin.multiplatform")
  apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

  repositories {
    mavenCentral()
  }

  kotlin {
    jvm {
      compilations.all {
        kotlinOptions.jvmTarget = "16"
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all")
      }

      testRuns["test"].executionTask.configure {
        useJUnitPlatform()
      }
    }

    sourceSets {
      all {
        languageSettings {
          optIn("kotlin.RequiresOptIn")
          optIn("kotlin.contracts.ExperimentalContracts")
        }
      }
      
      val commonMain by getting {
        dependencies {
          implementation("io.ktor:ktor-network:1.6.4")
          implementation("com.benasher44:uuid:0.3.1")
          implementation("net.benwoodworth.knbt:knbt:0.11.1")
          implementation("io.github.microutils:kotlin-logging:1.12.5")
          implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
          implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
        }
      }
      val commonTest by getting {
        dependencies {
          implementation(kotlin("test-common"))
        }
      }

      val jvmMain by getting {
        dependencies {
          implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.2")
        }
      }
      val jvmTest by getting {
        dependencies {
          implementation(kotlin("test-junit5"))
        }
      }
    }
  }
}
