import java.net.URL
import kotlin.String

class HtmlPageReader(private val url: String) : PageReader {

    override fun read() = HtmlPage(url, URL(url).readText())
}