import org.jsoup.nodes.Document
import kotlin.String

class JsoupDocumentPage(val baseUrl: String, override val content: Document): Page<Document>(content = content)