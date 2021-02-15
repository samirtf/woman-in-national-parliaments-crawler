import java.text.SimpleDateFormat
import java.util.*

data class ArchiveUrl(val path: String, val date: Date)

fun Href.toArchiveUrl(): ArchiveUrl {
    val extensionLength = 4;
    val end = path.length - extensionLength
    val dateStringLength = 6
    val start = path.length - extensionLength - dateStringLength
    val dateString = this.path.substring(start, end)
    val date = SimpleDateFormat("ddMMyy").parse(dateString)
    return ArchiveUrl(path, date)
}