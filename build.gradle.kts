plugins {
  kotlin("jvm") version "2.3.21" apply false
}

allprojects {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
  }
}