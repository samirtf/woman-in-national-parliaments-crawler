import java.util.*

data class ArchiveUrl(val path: String, val date: Date)

fun Href.toArchiveUrl(): ArchiveUrl {
    val date = extractDateFromClassifUrl(path)
    return ArchiveUrl(path, date)
}