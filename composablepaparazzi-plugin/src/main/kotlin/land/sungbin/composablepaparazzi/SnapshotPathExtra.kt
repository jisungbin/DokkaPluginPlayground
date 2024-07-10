package land.sungbin.composablepaparazzi

import okio.Path
import org.jetbrains.dokka.model.properties.ExtraProperty
import org.jetbrains.dokka.pages.ContentNode

@JvmInline
value class SnapshotPathExtra(val path: Path) : ExtraProperty<ContentNode> {
  override val key: ExtraProperty.Key<ContentNode, *> get() = Key

  companion object Key : ExtraProperty.Key<ContentNode, SnapshotPathExtra>
}
