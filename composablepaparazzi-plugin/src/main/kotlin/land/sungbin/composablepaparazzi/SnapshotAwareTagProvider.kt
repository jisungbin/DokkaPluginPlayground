package land.sungbin.composablepaparazzi

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.base.transformers.pages.tags.CustomTagContentProvider
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder.DocumentableContentBuilder
import org.jetbrains.dokka.model.doc.CustomTagWrapper
import org.jetbrains.dokka.model.doc.Text
import org.jetbrains.dokka.model.firstMemberOfTypeOrNull
import org.jetbrains.dokka.pages.TextStyle

object SnapshotAwareTagProvider : CustomTagContentProvider {
  override fun isApplicable(customTag: CustomTagWrapper): Boolean =
    customTag.name == SnapshotImageProvider.TAG_NAME_ANNOTATION ||
      customTag.name == SnapshotImageProvider.TAG_SIZE_ANNOTATION

  override fun DocumentableContentBuilder.contentForDescription(
    sourceSet: DokkaConfiguration.DokkaSourceSet,
    customTag: CustomTagWrapper,
  ) {
    if (customTag.name == SnapshotImageProvider.TAG_NAME_ANNOTATION) {
      val tagContent = customTag.firstMemberOfTypeOrNull<Text>()?.body?.trimIndent() ?: return

      breakLine()
      group {
        text("Snapshot names", sourceSets = setOf(sourceSet), styles = setOf(TextStyle.Bold))
        unorderedList {
          tagContent.split(',').forEach { name ->
            item { text(name) }
          }
        }
      }
    }
  }
}