import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.plugability.PluginApiPreviewAcknowledgement

@Suppress("unused")
class ComposablePaparazziPlugin : DokkaPlugin() {
  val paparazziSignatureProvider by extending {
    (plugin<DokkaBase>().signatureProvider
      providing ::KotlinWithSnapshotAwareComposableSignatureProvider
      override plugin<DokkaBase>().kotlinSignatureProvider)
  }

  val snapshotCopyAction by extending {
    CoreExtensions.postActions providing ::SnapshotCopyAction
  }

  override fun pluginApiPreviewAcknowledgement() = PluginApiPreviewAcknowledgement

  companion object {
    const val PLUGIN_NAME = "land.sungbin.dokka.ComposablePaparazziPlugin"
  }
}
