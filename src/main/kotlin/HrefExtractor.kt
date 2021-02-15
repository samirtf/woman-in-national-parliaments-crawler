import java.util.regex.Pattern

class HrefExtractor(private val content: String) {
    fun extract(): List<String> {
        val hrefRegex = "href=\"([^\"]*)\""
        return extractByRegex(content, hrefRegex).map { it.substring(6, it.length - 1) }
    }
}