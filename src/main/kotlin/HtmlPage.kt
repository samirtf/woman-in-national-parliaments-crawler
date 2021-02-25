class HtmlPage(val baseUrl: String, override val content: String):
    Page<String>(content = content)