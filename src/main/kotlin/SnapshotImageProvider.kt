import okio.FileSystem
import okio.Path
import org.jetbrains.dokka.base.renderers.isImage
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DFunction

class SnapshotImageProvider(
  private val path: Path,
  private val fs: FileSystem = FileSystem.SYSTEM,
) {
  private var pathCaches: List<Path>? = null

  init {
    require(fs.exists(path)) { "Snapshot directory '$path' does not exist" }
    require(fs.metadata(path).isDirectory) { "Snapshot directory '$path' is not a directory" }
  }

  fun getPath(fn: DFunction): Path? {
    pathCaches?.let { pathCaches -> return pathCaches.findPath(fn.name) }
    return fs.listRecursively(path).toList().also { pathCaches = it }.findPath(fn.name)
  }

  private fun List<Path>.findPath(name: String): Path? {
    for (i in indices) {
      val path = this[i]
      if (path.name.contains(name, ignoreCase = true) && path.name.isImage()) return path
    }
    return null
  }

  companion object {
    const val CONFIGURATION_PATH_KEY = "snapshotImageDir"

    fun Path.dri() = DRI(
      packageName = toString().substringBeforeLast('/', missingDelimiterValue = ""),
      classNames = toString().substringAfterLast('/'),
    )
  }
}
