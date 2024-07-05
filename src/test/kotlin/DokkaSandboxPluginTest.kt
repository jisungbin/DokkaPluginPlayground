import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.dokka.base.testApi.testRunner.BaseAbstractTest

class DokkaSandboxPluginTest : BaseAbstractTest() {
  @Test fun hideInternalAnnotatedFunctions() {
    val configuration = dokkaConfiguration {
      sourceSets {
        sourceSet {
          sourceRoots = listOf("src/main/kotlin/basic/Test.kt")
        }
      }
    }
    val hideInternalPlugin = DokkaSandboxPlugin()

    testInline(
      """
        /src/main/kotlin/basic/Test.kt
        package org.jetbrains.dokka.internal.test
        
        annotation class Internal
        
        fun shouldBeVisible() {}
        
        @Internal
        fun shouldBeExcludedFromDocumentation() {}
      """.trimIndent(),
      configuration = configuration,
      pluginOverrides = listOf(hideInternalPlugin),
    ) {
      preMergeDocumentablesTransformationStage = { modules ->
        val testModule = modules.single { it.name == "root" }
        val testPackage = testModule.packages.single { it.name == "org.jetbrains.dokka.internal.test" }

        val packageFunctions = testPackage.functions
        assertEquals(1, packageFunctions.size)
        assertEquals("shouldBeVisible", packageFunctions[0].name)
      }
    }
  }
}
