package land.sungbin.composablepaparazzi

import assertk.all
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.exists
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.prop
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.exists
import kotlin.io.path.walk
import kotlin.test.Test
import okio.Path.Companion.toPath
import org.jetbrains.dokka.model.withDescendants
import org.jetbrains.dokka.pages.ContentEmbeddedResource
import org.jetbrains.dokka.pages.MemberPageNode
import org.jetbrains.dokka.pages.RootPageNode

@OptIn(ExperimentalPathApi::class)
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
      val embeddeds = page.embeddedResources()
      assertThat(embeddeds).hasSize(1)
      assertThat(embeddeds.first()).all {
        prop(ContentEmbeddedResource::address).isEqualTo("snapshots/FakeSnapshot.png")
        prop(ContentEmbeddedResource::altText).isEqualTo("FakeSnapshot.png")
        prop(ContentEmbeddedResource::extra)
          .transform { it.allOfType<SnapshotPathExtra>() }
          .containsOnly(SnapshotPathExtra("src/test/resources/FakeSnapshot.png".toPath()))
      }
    },
    verifyOnPostStage = { root ->
      val current = root.walk().first { path -> path.endsWith("-fake-snapshot.html") }.parent
      assertThat(current.resolve("snapshots/FakeSnapshot.png")).exists()
    },
  )

  @Test fun composableFunctionDocumentedWithCustomSizedSnapshot() = test(
    "src/main/kotlin/Composable.kt",
    """
    package androidx.compose.runtime
    annotation class Composable
    """,
    "src/main/kotlin/FakeComposable.kt",
    """
    package androidx.compose.runtime
    /** @snapshotsize 100,100 */
    @Composable fun FakeSnapshot() = Unit
    """,
    verifyOnPagesGenerationStage = { page ->
      val embeddeds = page.embeddedResources()
      assertThat(embeddeds).hasSize(1)
      assertThat(embeddeds.first()).all {
        prop(ContentEmbeddedResource::address).isEqualTo("snapshots/FakeSnapshot.png")
        prop(ContentEmbeddedResource::altText).isEqualTo("FakeSnapshot.png")
        prop(ContentEmbeddedResource::extra)
          .transform { it.allOfType<SnapshotPathExtra>() }
          .containsOnly(SnapshotPathExtra("src/test/resources/FakeSnapshot.png".toPath()))
        prop(ContentEmbeddedResource::extra)
          .transform { it.allOfType<SnapshotSizeExtra>() }
          .containsOnly(SnapshotSizeExtra(width = "100", height = "100"))
      }
    },
    verifyOnPostStage = { root ->
      val current = root.walk().first { path -> path.endsWith("-fake-snapshot.html") }.parent
      assertThat(current.resolve("snapshots/FakeSnapshot.png")).exists()
    },
  )

  @Test fun composableFunctionDocumentedWithInvalidCustomSizedSnapshot() = test(
    "src/main/kotlin/Composable.kt",
    """
    package androidx.compose.runtime
    annotation class Composable
    """,
    "src/main/kotlin/FakeComposable.kt",
    """
    package androidx.compose.runtime
    /** @snapshotsize */
    @Composable fun FakeSnapshot() = Unit
    """,
    verifyOnPagesGenerationStage = { page ->
      val embeddeds = page.embeddedResources()
      assertThat(embeddeds).hasSize(1)
      assertThat(embeddeds.first()).all {
        prop(ContentEmbeddedResource::address).isEqualTo("snapshots/FakeSnapshot.png")
        prop(ContentEmbeddedResource::altText).isEqualTo("FakeSnapshot.png")
        prop(ContentEmbeddedResource::extra)
          .transform { it.allOfType<SnapshotSizeExtra>() }
          .isEmpty()
        prop(ContentEmbeddedResource::extra)
          .transform { it.allOfType<SnapshotPathExtra>() }
          .containsOnly(SnapshotPathExtra("src/test/resources/FakeSnapshot.png".toPath()))
      }
    },
    verifyOnPostStage = { root ->
      val current = root.walk().first { path -> path.endsWith("-fake-snapshot.html") }.parent
      assertThat(current.resolve("snapshots/FakeSnapshot.png")).exists()
    },
  )

  @Test fun composableFunctionDocumentedWithSnapshotButSnapshotDoesNotExists() = test(
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
      val current = root.walk().first { path -> path.endsWith("-fake-snapshot2.html") }.parent

      // TODO Assert<Path>.doesNotExist()
      //  when https://github.com/willowtreeapps/assertk/pull/542 is released
      assertThat(current.resolve("snapshots/FakeSnapshot2.png").exists()).isFalse()
    },
  )

  @Test fun regularFunctionDocumentedWithoutSnapshot() = test(
    "src/main/kotlin/Function.kt",
    "fun FakeSnapshot() = Unit",
    verifyOnPagesGenerationStage = { page ->
      assertThat(page.embeddedResources()).isEmpty()
    },
    verifyOnPostStage = { root ->
      val current = root.walk().first { path -> path.endsWith("-fake-snapshot.html") }.parent

      // TODO Assert<Path>.doesNotExist()
      //  when https://github.com/willowtreeapps/assertk/pull/542 is released
      assertThat(current.resolve("snapshots/FakeSnapshot.png").exists()).isFalse()
    },
  )

  private fun RootPageNode.embeddedResources(): List<ContentEmbeddedResource> =
    withDescendants().filterIsInstance<MemberPageNode>()
      .flatMap { member -> member.content.withDescendants().filterIsInstance<ContentEmbeddedResource>() }
      .toList()
}
