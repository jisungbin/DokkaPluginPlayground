import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.transformers.documentables.SuppressedByConditionDocumentableFilterTransformer
import org.jetbrains.dokka.model.Annotations
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.properties.WithExtraProperties
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.plugability.PluginApiPreviewAcknowledgement

@Suppress("unused")
class DokkaSandboxPlugin : DokkaPlugin() {
  val internalHidingTransformer by extending {
    plugin<DokkaBase>().preMergeDocumentableTransformer providing ::HideInternalApiTransformer
  }

  override fun pluginApiPreviewAcknowledgement() = PluginApiPreviewAcknowledgement
}

class HideInternalApiTransformer(context: DokkaContext) : SuppressedByConditionDocumentableFilterTransformer(context) {
  override fun shouldBeSuppressed(d: Documentable): Boolean {
    val annotations: List<Annotations.Annotation> =
      (d as? WithExtraProperties<*>)
        ?.extra
        ?.allOfType<Annotations>()
        ?.flatMap { it.directAnnotations.values.flatten() }
        .orEmpty()

    return annotations.any(::isInternalAnnotation)
  }

  private fun isInternalAnnotation(annotation: Annotations.Annotation): Boolean =
    annotation.dri.packageName == "org.jetbrains.dokka.internal.test" &&
      annotation.dri.classNames == "Internal"
}
