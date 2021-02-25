import org.jsoup.nodes.Document
import table.Table
import tableextractor.NoTitleTableExtractor
import tableextractor.WithTitleTableExtractor
import tableextractor.WithTitleTableParsingException
import java.io.File


fun main() {
    println("Start.")
    val baseUrl = "http://archive.ipu.org"
    // http://archive.ipu.org/wmn-e/arc/classif010197.htm
    var archivesUrls = extractArchivesUrls(baseUrl)
    //archivesUrls = archivesUrls.subList(archivesUrls.size - 10, archivesUrls.size)
    println(archivesUrls)
    writeAllArchivesInASingleFile(baseUrl, archivesUrls)
    println("End.")
}

private fun writeAllArchivesInASingleFile(baseUrl: String, archivesUrls: List<ArchiveUrl>) {
    archivesUrls.forEach {
        val currentUrl = "$baseUrl${it.path}"
        writeArchiveToFile(currentUrl)
    }
}

fun writeArchiveToFile(currentUrl: String) {
    try {
        val pageReader = JsoupDocumentPageReader(currentUrl)
        val page = pageReader.read()
        val table = extractTableFromDoc(page.content)

        // fill and index table
//        val filledAndIndexedTable = fillArchiveTableWithExtrasAndIndex(table, currentUrl)
//        filledAndIndexedTable.let {
//            println(filledAndIndexedTable)
//            CsvTableWriter(file = File("./content.csv"), includeTitle = false, includeHeader = false)
//                .write(filledAndIndexedTable)
//        }

        table.let {
            println(it)
            CsvTableWriter(file = File("./content.csv"), includeTitle = false, includeHeader = false)
                .write(it)
        }
    } catch (exc: Exception) {
        exc.printStackTrace()
        print("file or page not found exception: ")
        println(exc.message)
    }
}

private fun extractArchivesUrls(baseUrl: String): List<ArchiveUrl> {
    val url = "$baseUrl/wmn-e/classif-arc.htm"
    val pageReader: PageReader<String> = HtmlPageReader(url)
    val pageContent = pageReader.read()
    val hrefs = HrefExtractor(url, pageContent).extract()
    val archiveHrefs = hrefs.filter { href -> href.path.contains("/arc/", ignoreCase = true) }
    return archiveHrefs.map { it.toArchiveUrl() }
}

fun extractTableFromDoc(doc: Document): Table {
    return try { createTableFromDoc(doc) }
    catch (exc: Exception) {
        exc.printStackTrace()
        throw TableExtractionException()
    }
}


private fun createTableFromDoc(doc: Document): Table {
    return try {
        WithTitleTableExtractor(doc).extract()
    } catch (exc: WithTitleTableParsingException) {
        NoTitleTableExtractor(doc).extract()
    }
}



