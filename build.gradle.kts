plugins {
  kotlin("jvm") version "2.0.20" apply false
}

allprojects {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
  }
}