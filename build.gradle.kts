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

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
  kotlin("multiplatform") version "1.6.21" apply false
  kotlin("plugin.serialization") version "1.6.21" apply false
  id("org.jlleitschuh.gradle.ktlint") version "10.2.1" apply false
  id("io.gitlab.arturbosch.detekt") version "1.19.0" apply false
}

group = "me.gabrielleeg1"
version = "0.0.1-dev"

subprojects {
  apply(plugin = "org.jetbrains.kotlin.multiplatform")
  apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
  apply(plugin = "org.jlleitschuh.gradle.ktlint")
  apply(plugin = "io.gitlab.arturbosch.detekt")

  repositories {
    mavenCentral()
  }

  configure<KtlintExtension> {
    android.set(false)
    additionalEditorconfigFile.set(rootProject.file(".editorconfig"))
  }

  configure<DetektExtension> {
    buildUponDefaultConfig = true
    allRules = false

    config = files("${rootProject.projectDir}/config/detekt.yml")
    baseline = file("${rootProject.projectDir}/config/baseline.xml")
  }

  configure<KotlinMultiplatformExtension> {
    jvm {
      compilations.all {
        kotlinOptions.jvmTarget = "16"
        kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all", "-Xcontext-receivers")
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
          implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
          implementation("io.ktor:ktor-network:1.6.4")
          implementation("com.benasher44:uuid:0.3.1")
          implementation("net.benwoodworth.knbt:knbt:0.11.1")
          implementation("org.jetbrains.kotlinx:atomicfu:0.17.2")
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
          implementation("org.apache.logging.log4j:log4j-api-kotlin:1.1.0")
          implementation("org.apache.logging.log4j:log4j-api:2.17.2")
          implementation("org.apache.logging.log4j:log4j-core:2.17.2")

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
