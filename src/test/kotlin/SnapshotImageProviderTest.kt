
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.mockk.every
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

class SnapshotImageProviderTest {
  private var fs: FakeFileSystem? = null

  @BeforeTest fun prepare() {
    fs = FakeFileSystem()
  }

  @AfterTest fun cleanup() {
    fs?.checkNoOpenFiles()
    fs = null
  }

  @Test fun snapshotPathShouldBeExist() {
    val fs = fs!!
    val path = "snapshot".toPath()

    assertFailure { SnapshotImageProvider(path, fs) }
      .hasMessage("Snapshot directory '$path' does not exist")
  }

  @Test fun snapshotPathShouldBeDirectory() {
    val fs = fs!!
    val path = "helloworld.txt".toPath().also { fs.write(it) {} }

    assertFailure { SnapshotImageProvider(path, fs) }
      .hasMessage("Snapshot directory '$path' is not a directory")
  }

  @Test fun takingSnapshotOfComposableFromDeepPath() {
    val fs = fs!!
    val path = "a/b/c/d/e/f/g/h".toPath().also {
      fs.createDirectories(it)
      fs.write(it.resolve("Hello.png")) {}
    }
    val provider = SnapshotImageProvider(path, fs)
    val result = provider.getPath(mockk { every { name } returns "Hello" })

    assertThat(result).isEqualTo(path.resolve("Hello.png"))
  }

  @Test fun returnsNullIfSnapshotDoesNotExist() {
    val fs = fs!!
    val provider = SnapshotImageProvider("/".toPath(), fs)
    val result = provider.getPath(mockk { every { name } returns "World" })

    assertThat(result).isNull()
  }
}