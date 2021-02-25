import java.net.URL

class HtmlPageReader(private val url: String) : PageReader<String> {

    override fun read(): Page<String> {
        val content: String = URL(url).readText()
        return HtmlPage(url, content)
    }
}