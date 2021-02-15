

fun main() {
    val baseUrl = "http://archive.ipu.org"
    val url = "$baseUrl/wmn-e/classif-arc.htm"
    val pageReader: PageReader = HtmlPageReader(url)
    val pageContent = pageReader.read()
    val hrefs = HrefExtractor(pageContent).extract()
    hrefs.forEach { println("$baseUrl$it") }

}