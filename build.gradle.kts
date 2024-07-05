import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm") version "2.0.0"
}

repositories {
  mavenCentral()
  gradlePluginPortal()
  google()
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
    optIn.addAll(
      "org.jetbrains.dokka.InternalDokkaApi",
      "org.jetbrains.dokka.plugability.DokkaPluginApiPreview",
    )
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

dependencies {
  val dokkaVersion = "1.9.20"

  compileOnly("org.jetbrains.dokka:dokka-core:$dokkaVersion")
  compileOnly("org.jetbrains.dokka:analysis-kotlin-api:$dokkaVersion")
  implementation("org.jetbrains.dokka:dokka-base:$dokkaVersion")

  testImplementation(kotlin("test-junit5"))
  testImplementation("org.jetbrains.dokka:dokka-test-api:$dokkaVersion")
  testImplementation("org.jetbrains.dokka:dokka-base-test-utils:$dokkaVersion")
}
