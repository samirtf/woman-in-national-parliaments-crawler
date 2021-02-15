import java.net.URL

class HtmlPageReader(private val url: String) : PageReader {

    override fun read() = URL(url).readText()

}