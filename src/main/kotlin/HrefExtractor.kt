class HrefExtractor(private val url: String, private val page: Page<String>) {
    fun extract(): List<Href> {
        val hrefRegex = "href=\"([^\"]*)\""
        return extractByRegex(page.content, hrefRegex).map { Href(it.substring(6, it.length - 1)) }
    }
}