package land.sungbin.composablepaparazzi

import kotlinx.html.FlowContent
import kotlinx.html.img
import land.sungbin.composablepaparazzi.SnapshotAwareKotlinSignatureProvider.Companion.dokkaSnapshotPathFor
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.jetbrains.dokka.base.renderers.html.HtmlRenderer
import org.jetbrains.dokka.base.renderers.isImage
import org.jetbrains.dokka.pages.ContentEmbeddedResource
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.plugability.DokkaContext

class SnapshotAwareHtmlRenderer(
  context: DokkaContext,
  private val fs: FileSystem = FileSystem.SYSTEM,
) : HtmlRenderer(context) {
  private val logger = context.logger
  private val outputPath = context.configuration.outputDir.toOkioPath()

  override fun FlowContent.buildResource(node: ContentEmbeddedResource, pageContext: ContentPage) {
    val overrideSize = node.extra.allOfType<SnapshotSizeExtra>().firstOrNull()
    val providedSnapshot = node.extra.allOfType<SnapshotPathExtra>().firstOrNull()

    if (node.isImage()) {
      img(src = node.address, alt = node.altText) {
        if (overrideSize != null) {
          width = overrideSize.width
          height = overrideSize.height
        }
      }
    } else {
      logger.error("Unrecognized resource address: ${node.address}")
    }

    if (providedSnapshot != null) {
      val destination = run {
        val current = locationProvider.resolve(pageContext, skipExtension = true) ?: run {
          logger.error("Failed to resolve the location for the current node: ${node.dci}")
          return
        }
        outputPath.resolve(current.substringBeforeLast('/')).resolve(dokkaSnapshotPathFor(providedSnapshot.path))
      }
      destination.parent?.let(fs::createDirectories)
      fs.copy(providedSnapshot.path, destination)
      logger.debug("Copied ${providedSnapshot.path} to $destination")
    }
  }
}