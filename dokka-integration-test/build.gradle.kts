plugins {
  kotlin("jvm")
  id("org.jetbrains.dokka") version "2.2.0"
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
