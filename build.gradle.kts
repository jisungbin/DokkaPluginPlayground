plugins {
  kotlin("jvm") version "2.0.0" apply false
}

allprojects {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
  }
}