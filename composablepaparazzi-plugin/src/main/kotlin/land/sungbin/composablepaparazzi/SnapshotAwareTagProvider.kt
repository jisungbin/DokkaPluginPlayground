package land.sungbin.composablepaparazzi

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.base.transformers.pages.tags.CustomTagContentProvider
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder.DocumentableContentBuilder
import org.jetbrains.dokka.model.doc.CustomTagWrapper

object SnapshotAwareTagProvider : CustomTagContentProvider {
  override fun isApplicable(customTag: CustomTagWrapper): Boolean =
    customTag.name == SnapshotImageProvider.TAG_NAME_ANNOTATION ||
      customTag.name == SnapshotImageProvider.TAG_SIZE_ANNOTATION

  override fun DocumentableContentBuilder.contentForDescription(
    sourceSet: DokkaConfiguration.DokkaSourceSet,
    customTag: CustomTagWrapper,
  ) {
    comment(customTag.root, sourceSets = setOf(sourceSet))
  }
}