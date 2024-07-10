package land.sungbin.composablepaparazzi

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isFalse
import kotlin.io.path.exists
import kotlin.test.Test
import org.jetbrains.dokka.model.withDescendants
import org.jetbrains.dokka.pages.ContentEmbeddedResource
import org.jetbrains.dokka.pages.MemberPageNode
import org.jetbrains.dokka.pages.RootPageNode

class ComposablePaparazziPluginTest : AbstractComposePaparazziTest() {
  @Test fun composableFunctionDocumentedWithSnapshot() = test(
    "src/main/kotlin/Composable.kt",
    """
    package androidx.compose.runtime
    annotation class Composable
    """,
    "src/main/kotlin/FakeComposable.kt",
    """
    package androidx.compose.runtime
    @Composable fun FakeSnapshot() = Unit
    """,
    verifyOnPagesGenerationStage = { page ->
      /*val embeddeds = page.embeddedResources()
      assertThat(embeddeds).hasSize(1)
      assertThat(embeddeds.first()).all {
        prop(ContentEmbeddedResource::address).isEqualTo("images/snapshot/FakeSnapshot.png")
        prop(ContentEmbeddedResource::altText).isEqualTo("FakeSnapshot.png")
      }*/
    },
    verifyOnPostStage = { root ->
      // assertThat(root.resolve("images/snapshot/FakeSnapshot.png")).exists()
    },
  )

  @Test fun composableFunctionShouldDocumentedWithSnapshotButSnapshotDoesNotExists() = test(
    "src/main/kotlin/Composable.kt",
    """
    package androidx.compose.runtime
    annotation class Composable
    """,
    "src/main/kotlin/FakeComposable2.kt",
    """
    package androidx.compose.runtime
    @Composable fun FakeSnapshot2() = Unit
    """,
    verifyOnPagesGenerationStage = { page ->
      assertThat(page.embeddedResources()).isEmpty()
    },
    verifyOnPostStage = { root ->
      // TODO Assert<Path>.doesNotExist()
      //  when https://github.com/willowtreeapps/assertk/pull/542 is released
      assertThat(root.resolve("images/snapshot/FakeSnapshot2.png").exists()).isFalse()
    },
  )

  @Test fun regularFunctionDocumentedWithoutSnapshot() = test(
    "src/main/kotlin/Function.kt",
    "fun FakeSnapshot() = Unit",
    verifyOnPagesGenerationStage = { page ->
      assertThat(page.embeddedResources()).isEmpty()
    },
    verifyOnPostStage = { root ->
      // TODO Assert<Path>.doesNotExist()
      //  when https://github.com/willowtreeapps/assertk/pull/542 is released
      assertThat(root.resolve("images/snapshot/FakeSnapshot.png").exists()).isFalse()
    },
  )

  private fun RootPageNode.embeddedResources(): List<ContentEmbeddedResource> =
    withDescendants().filterIsInstance<MemberPageNode>()
      .flatMap { member -> member.content.withDescendants().filterIsInstance<ContentEmbeddedResource>() }
      .toList()
}
