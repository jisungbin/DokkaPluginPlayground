import SnapshotImageProvider.Companion.dri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.contracts.contract
import okio.Path
import okio.Path.Companion.toPath
import org.jetbrains.dokka.base.signatures.KotlinSignatureProvider
import org.jetbrains.dokka.base.signatures.KotlinSignatureUtils.annotations
import org.jetbrains.dokka.base.signatures.SignatureProvider
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.toDisplaySourceSets
import org.jetbrains.dokka.pages.ContentEmbeddedResource
import org.jetbrains.dokka.pages.ContentKind
import org.jetbrains.dokka.pages.ContentNode
import org.jetbrains.dokka.pages.DCI
import org.jetbrains.dokka.plugability.DokkaContext

class KotlinWithSnapshotAwareComposableSignatureProvider(context: DokkaContext) : SignatureProvider {
  private val delegator = KotlinSignatureProvider(context)
  private val logger = context.logger
  private val snapshotImageProvider: SnapshotImageProvider

  private val provided = mutableListOf<Path>()
  val providedPaths: List<Path> get() = provided

  init {
    val configuration = context.configuration.pluginsConfiguration.find { pluginsConfiguration ->
      pluginsConfiguration.fqPluginName == ComposablePaparazziPlugin.PLUGIN_NAME
    }
    checkNotNull(configuration) { "The 'ComposablePaparazziPlugin' Dokka configuration is missing." }

    val values = Gson().fromJson<Map<String, String>>(configuration.values, object : TypeToken<Map<String, String>>() {}.type)
    val path = checkNotNull(values[SnapshotImageProvider.CONFIGURATION_PATH_KEY]) {
      "The 'snapshotImageDir' field in the 'ComposablePaparazziPlugin' Dokka configuration is missing."
    }

    snapshotImageProvider = SnapshotImageProvider(path.toPath())
  }

  override fun signature(documentable: Documentable): List<ContentNode> {
    val original = delegator.signature(documentable)
    if (!isComposable(documentable)) return original
    logger.debug("Generating snapshot-aware signature for ${documentable.dri}")

    val snapshotPath = snapshotImageProvider.getPath(documentable)
    if (snapshotPath == null) {
      logger.debug("No snapshot found for ${documentable.dri}")
      return original
    }

    // TODO supports sourceSets for snapshot path
    val snapshotContentNode = ContentEmbeddedResource(
      address = dokkaSnapshotPathFor(snapshotPath),
      altText = snapshotPath.segments.last(),
      dci = DCI(setOf(snapshotPath.dri()), ContentKind.Symbol),
      sourceSets = documentable.sourceSets.toDisplaySourceSets(),
    )
    return original.toMutableList().apply { add(snapshotContentNode) }.also {
      provided.add(snapshotPath)
      logger.debug("Snapshot found for ${documentable.dri}: $snapshotPath")
    }
  }

  private fun isComposable(documentable: Documentable): Boolean {
    contract { returns(true) implies (documentable is DFunction) }
    if (documentable !is DFunction) return false
    val found = documentable.annotations().values.flatten().find { annotation ->
      annotation.dri.packageName == COMPOSABLE_ANNOTATION.packageName &&
        annotation.dri.classNames == COMPOSABLE_ANNOTATION.classNames
    }
    return found != null
  }

  companion object {
    private val COMPOSABLE_ANNOTATION = DRI(packageName = "androidx.compose.runtime", classNames = "Composable")

    @Suppress("NOTHING_TO_INLINE")
    inline fun dokkaSnapshotPathFor(actual: Path): String = "images/snapshot/${actual.name}"
  }
}
