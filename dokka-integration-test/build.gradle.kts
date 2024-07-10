plugins {
  kotlin("jvm")
  id("org.jetbrains.dokka") version "1.9.20"
}

tasks.dokkaHtml {
  outputDirectory = projectDir.resolve("output")
  pluginsMapConfiguration = mapOf(
    "land.sungbin.composablepaparazzi.ComposablePaparazziPlugin" to """
      |{
      |  "snapshotImageDir": "${projectDir.resolve("snapshots")}"
      |}
    """.trimMargin()
  )
}

dependencies {
  dokkaPlugin(project(":composablepaparazzi-plugin"))
}
