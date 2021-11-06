plugins {
    kotlin("multiplatform") version "1.5.10"
}

group = "com.gabrielleeg1"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()

        compilations.all {
            kotlinOptions.jvmTarget = "16"
        }

        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting

        val jvmMain by getting
        val jvmTest by getting
    }
}
