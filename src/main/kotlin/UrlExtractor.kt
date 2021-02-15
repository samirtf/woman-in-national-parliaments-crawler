import java.util.regex.Pattern

class UrlExtractor(private val content: String) {
    fun extract(): List<String> {
        val urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"
        return extractByRegex(content, urlRegex)
    }
}