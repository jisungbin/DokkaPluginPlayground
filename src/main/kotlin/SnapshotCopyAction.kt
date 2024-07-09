
import KotlinWithSnapshotAwareComposableSignatureProvider.Companion.dokkaSnapshotPathFor
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.plugin
import org.jetbrains.dokka.plugability.querySingle
import org.jetbrains.dokka.renderers.PostAction
import org.jetbrains.dokka.utilities.cast

class SnapshotCopyAction(
  context: DokkaContext,
  private val fs: FileSystem = FileSystem.SYSTEM,
) : PostAction {
  private val logger = context.logger
  private val dokkaPath = context.configuration.outputDir.toOkioPath()

  private val base by lazy { context.plugin<DokkaBase>() }
  private val provided by lazy {
    base.querySingle { signatureProvider }
      .cast<KotlinWithSnapshotAwareComposableSignatureProvider>()
      .providedPaths
  }

  override fun invoke() {
    logger.debug("Copying snapshot images to the output directory...")
    for (i in provided.indices) {
      val snapshot = provided[i]
      val destination = dokkaPath.resolve(dokkaSnapshotPathFor(snapshot).toPath())
      fs.createDirectories(destination.parent!!)
      fs.copy(snapshot, destination)
      logger.debug("Copied $snapshot to $destination")
    }
    logger.debug("Snapshot images copied.")
  }
}