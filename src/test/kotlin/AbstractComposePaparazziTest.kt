
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.absolutePathString
import kotlin.io.path.createParentDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.DokkaConfiguration.SerializationFormat
import org.jetbrains.dokka.PluginConfigurationImpl
import org.jetbrains.dokka.base.testApi.testRunner.BaseAbstractTest
import org.jetbrains.dokka.base.testApi.testRunner.BaseTestBuilder
import org.jetbrains.dokka.pages.RootPageNode
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.testApi.logger.TestLogger
import org.jetbrains.dokka.utilities.DokkaConsoleLogger
import org.jetbrains.dokka.utilities.DokkaLogger
import org.jetbrains.dokka.utilities.LoggingLevel

// SignatureProvider.signature() calls are originally made twice for the same Documentable.
// I don't know why, but I've seen it called twice in the test code in Kotlin/Dokka.
abstract class AbstractComposePaparazziTest(
  logger: TestLogger = TestLogger(DokkaConsoleLogger(LoggingLevel.DEBUG)),
) : BaseAbstractTest(logger) {
  protected fun test(
    vararg pathAndContents: String,
    cleanupOutput: Boolean = true,
    verifyOnPostStage: (Path) -> Unit = {},
    verifyOnPagesGenerationStage: (RootPageNode) -> Unit,
  ) {
    require(pathAndContents.size % 2 == 0) { "pathAndContents should have even number of elements" }

    val configuration = dokkaConfiguration {
      sourceSets {
        sourceSet {
          sourceRoots = listOf("src/main/kotlin")
        }
      }
      pluginsConfigurations = mutableListOf(
        PluginConfigurationImpl(
          fqPluginName = ComposablePaparazziPlugin.PLUGIN_NAME,
          serializationFormat = SerializationFormat.JSON,
          values = """
          {
            "${SnapshotImageProvider.CONFIGURATION_PATH_KEY}": "src/test/resources"
          }
        """.trimIndent(),
        ),
      )
    }

    withTempDirectory(cleanUpAfterUse = cleanupOutput) { tempDir ->
      if (!cleanupOutput) logger.info("Output will be generated under: ${tempDir.absolutePathString()}")

      pathAndContents.asList().materializeFiles(tempDir.toAbsolutePath())

      val tempDirAsFile = tempDir.toFile()
      val newConfiguration = configuration.copy(
        outputDir = tempDir.toFile(),
        sourceSets = configuration.sourceSets.map { sourceSet ->
          sourceSet.copy(
            sourceRoots = sourceSet.sourceRoots.map { file -> tempDirAsFile.resolve(file) }.toSet(),
            suppressedFiles = sourceSet.suppressedFiles.map { file -> tempDirAsFile.resolve(file) }.toSet(),
            sourceLinks = sourceSet.sourceLinks.map { link ->
              link.copy(localDirectory = tempDirAsFile.resolve(link.localDirectory).absolutePath)
            }.toSet(),
            includes = sourceSet.includes.map { file -> tempDirAsFile.resolve(file) }.toSet(),
          )
        }
      )
      runTests(
        configuration = newConfiguration,
        pluginOverrides = listOf(ComposablePaparazziPlugin()),
        testLogger = logger,
      ) {
        pagesGenerationStage = verifyOnPagesGenerationStage
      }
      verifyOnPostStage(tempDir)
    }
  }

  private inline fun runTests(
    configuration: DokkaConfiguration,
    pluginOverrides: List<DokkaPlugin>,
    testLogger: DokkaLogger = logger,
    block: BaseTestBuilder.() -> Unit,
  ) {
    val testMethods = testBuilder().apply(block).build()
    dokkaTestGenerator(configuration, testLogger, testMethods, pluginOverrides).generate()
  }

  private fun List<String>.materializeFiles(root: Path) {
    for (index in indices step 2) {
      val path = get(index)
      val content = get(index + 1).trimIndent()
      val file = root.resolve(path.removePrefix("/"))

      file.createParentDirectories()
      file.writeText(content)
    }
  }

  @OptIn(ExperimentalPathApi::class)
  private inline fun withTempDirectory(cleanUpAfterUse: Boolean, block: (tempDirectory: Path) -> Unit) {
    val tempDir = createTempDirectory(prefix = "dokka-test")
    try {
      block(tempDir)
    } finally {
      if (cleanUpAfterUse) {
        tempDir.deleteRecursively()
      }
    }
  }
}