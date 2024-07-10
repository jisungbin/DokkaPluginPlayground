import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm")
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_17
    optIn.addAll(
      "org.jetbrains.dokka.InternalDokkaApi",
      "org.jetbrains.dokka.plugability.DokkaPluginApiPreview",
      "kotlin.contracts.ExperimentalContracts",
    )
  }
}

tasks.test {
  useJUnitPlatform()
}

dependencies {
  // TODO https://github.com/Kotlin/dokka/issues/2812
  val dokkaVersion = "1.9.20"

  compileOnly("org.jetbrains.dokka:dokka-core:$dokkaVersion")
  implementation("org.jetbrains.dokka:dokka-base:$dokkaVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-html:0.11.0")
  implementation("com.google.code.gson:gson:2.11.0")
  implementation("com.squareup.okio:okio:3.9.0")

  testImplementation(kotlin("test-junit5"))
  testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
  testImplementation("com.squareup.okio:okio-fakefilesystem:3.9.0")
  testImplementation("org.jetbrains.dokka:dokka-test-api:$dokkaVersion")
  testImplementation("org.jetbrains.dokka:dokka-base-test-utils:$dokkaVersion")
}

