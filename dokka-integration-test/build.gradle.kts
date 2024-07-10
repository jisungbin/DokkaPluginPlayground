plugins {
  kotlin("jvm")
  id("org.jetbrains.dokka") version "1.9.20"
}

val removeOldOutputTask = tasks.create<Delete>("removeOldOutput") {
  delete(file("output"))
}

tasks.dokkaHtml {
  dependsOn(removeOldOutputTask)
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
