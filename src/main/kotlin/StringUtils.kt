import java.text.SimpleDateFormat
import java.util.*

fun extractDateFromClassifUrl(url: String): Date {
        val extensionLength = 4;
        val end = url.length - extensionLength
        val dateStringLength = 6
        val start = url.length - extensionLength - dateStringLength
        val dateString = url.substring(start, end)
        return SimpleDateFormat("ddMMyy").parse(dateString)
}

object DateUtils {
        @JvmStatic
        fun toSimpleString(date: Date) : String {
                val format = SimpleDateFormat("dd/MM/yyyy")
                return format.format(date)
        }
}