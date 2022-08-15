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
import java.lang.System.getenv
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
  kotlin("multiplatform") version "1.7.0" apply false
  kotlin("plugin.serialization") version "1.7.0" apply false
  id("org.jlleitschuh.gradle.ktlint") version "10.3.0" apply false
  id("io.gitlab.arturbosch.detekt") version "1.19.0" apply false
  id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
  `maven-publish`
  signing
}

nexusPublishing {
  repositories {
    sonatype {
      nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
      snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))

      username.set(getenv("OSSRH_USERNAME"))
      password.set(getenv("OSSRH_PASSWORD"))
    }
  }
}

subprojects {
  apply(plugin = "org.jetbrains.kotlin.multiplatform")
  apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
  apply(plugin = "org.jlleitschuh.gradle.ktlint")
  apply(plugin = "io.gitlab.arturbosch.detekt")
  apply(plugin = "maven-publish")
  apply(plugin = "signing")

  repositories {
    mavenCentral()
  }

  configure<KtlintExtension> {
    version.set("0.45.2")
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
        kotlinOptions.jvmTarget = "1.8"
      }

      testRuns["test"].executionTask.configure {
        testLogging.showStandardStreams = true
        testLogging.exceptionFormat = TestExceptionFormat.FULL
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
          afterEvaluate {
            api(libs.ktx.datetime)
            api(libs.ktor.network)
            api(libs.uuid)
            api(libs.knbt)
            api(libs.ktx.atomicfu)
            api(libs.ktx.coroutines.core)
            api(libs.ktx.serialization.json)

            if (project.name != "andesite-shared") {
              api(projects.andesiteShared)
            }
          }
        }
      }
      val commonTest by getting {
        dependencies {
          afterEvaluate {
            implementation(libs.kt.test.common)
          }
        }
      }

      val jvmMain by getting {
        dependencies {
          afterEvaluate {
            api(libs.log4j.api)
            api(libs.log4j.core)
            api(libs.log4j.kotlin)

            api(libs.ktx.coroutines.jdk8)
          }
        }
      }
      val jvmTest by getting {
        dependencies {
          afterEvaluate {
            implementation(libs.kt.test.junit)
          }
        }
      }
    }
  }

  publishing {
    publications {
      withType<MavenPublication> {
        pom {
          licenses {
            license {
              name.set("Apache License, Version 2.0")
              url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
          }

          scm {
            url.set("https://github.com/gabrielleeg1/andesite")
            connection.set("git@github.com:gabrielleeg1/andesite.git")
          }

          developers {
            developer {
              id.set("gabrielleeg1")
              name.set("Gabrielle Guimarães de Oliveira")
              email.set("gabrielle1guim@gmail.com")
            }
          }
        }
      }
    }
  }

  if (getenv("OSSRH_SIGNING_KEY") != null) {
    signing {
      val keyId = getenv("OSSRH_SIGNING_KEY_ID")
      val key = getenv("OSSRH_SIGNING_KEY")
      val password = getenv("OSSRH_SIGNING_PASSWORD")

      useInMemoryPgpKeys(keyId, key, password)
    }
  }
}
