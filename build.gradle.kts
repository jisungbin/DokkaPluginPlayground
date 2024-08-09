plugins {
  kotlin("jvm") version "2.0.10" apply false
}

allprojects {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
  }
}