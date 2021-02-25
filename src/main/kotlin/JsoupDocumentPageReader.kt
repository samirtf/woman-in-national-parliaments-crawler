import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class JsoupDocumentPageReader(private val url: String) : PageReader<Document> {

    override fun read(): Page<Document> {
        try {
            val doc: Document = Jsoup.connect(url).get()
            return JsoupDocumentPage(url, doc)
        } catch (exc: Exception) {
            exc.printStackTrace()
            throw PageNotFoundException("Url [$url] not found")
        }
    }
}