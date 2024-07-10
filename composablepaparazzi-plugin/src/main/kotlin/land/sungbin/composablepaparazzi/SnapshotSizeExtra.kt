package land.sungbin.composablepaparazzi

import org.jetbrains.dokka.model.properties.ExtraProperty
import org.jetbrains.dokka.pages.ContentNode

data class SnapshotSizeExtra(val width: String, val height: String) : ExtraProperty<ContentNode> {
  override val key: ExtraProperty.Key<ContentNode, *> get() = Key

  companion object Key : ExtraProperty.Key<ContentNode, SnapshotPathExtra>
}