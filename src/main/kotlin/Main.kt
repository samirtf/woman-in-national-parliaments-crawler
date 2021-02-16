import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import table.Header
import table.Label
import table.Row
import table.Table
import java.io.FileNotFoundException


fun main() {
    val baseUrl = "http://archive.ipu.org"
    val url = "$baseUrl/wmn-e/classif-arc.htm"
    val pageReader: PageReader = HtmlPageReader(url)
    val pageContent = pageReader.read()
    val hrefs = HrefExtractor(url, pageContent).extract()
    val archiveHrefs = hrefs.filter { href -> href.path.contains("/arc/", ignoreCase = true) }
    //  archiveHrefs.forEach { println("$baseUrl${it.path}") }
    val archivesUrls = archiveHrefs.map { it.toArchiveUrl() }

    try {
        val archiveContent = HtmlPageReader("$baseUrl${archivesUrls[10].path}").read()
        //println(archiveContent.content)
        extractDataTable("$baseUrl${archivesUrls[10].path}")
    } catch (exc: FileNotFoundException) {
        println("file not found exception")
    }

//    archivesUrls.forEach { println("$it") }

}

fun extractDataTable(url: String) {

    val doc: Document
    try {
        doc = Jsoup.connect(url).get()
    } catch (exc: Exception) {
        return
    }

    val elements = doc.select("table[class=data]").select("tbody").select("tr")

    val table = extractTable(elements)
    println(table)
}

private fun extractTable(elements: Elements) =
    Table(extractTitle(elements), extractHeader(elements), extractTableData(elements))


private fun extractTitle(elements: Elements) =
    elements.first().getElementsByTag("b").first().text()

private fun extractHeader(elements: Elements): Header {
    val labels = with(elements) {
        val firstRowLabels = extractFirstRowLabels()
        val secondRowLabels = extractSecondRowLabels()
        val rankAndCountry = firstRowLabels.subList(0, 2)
        val lowerAndUpperHouses = firstRowLabels.subList(2, 4)
        val combinedLowerAndUpperHouseLabels = lowerAndUpperHouses * secondRowLabels
        return@with rankAndCountry + combinedLowerAndUpperHouseLabels
    }
    return Header(labels)
}

private fun extractTableData(allTableRows: Elements): List<Row> {
    return allTableRows.asSequence()
        .filterIndexed { index, _ -> index > 2 }
        .map { element -> tableRowToRow(element) }.toList()
}

private fun tableRowToRow(tableRow: Element): Row {
    val tableData = tableRow.select("td")
    val columnValues = tableData.map { element -> element.text() }
    return Row(columnValues)
}

private fun extractLabelsFromRow(row: Element): List<Label> {
    val columnNames = extractColumnNames(row)
    return columnNames.map { transformTextToLabel(it) }
}

private fun Elements.extractFirstRowLabels(): List<Label> = extractLabelsFromRow(this[1])
private fun Elements.extractSecondRowLabels(): List<Label> = extractLabelsFromRow(this[2])

private fun extractColumnNames(element: Element) =
    element.select("td").map { it.getElementsByTag("b").first().text() }

private fun transformTextToLabel(text: String): Label {
    val preprocessedText = text.trim().toLowerCase().replace("*", "").replace("%", "percent")
    val tokens = preprocessedText.split(" ")
    val transformed = if (tokens.size == 1) { tokens.first() } else { tokens.joinToString(separator = "_") }
    return Label(label = transformed, text)
}

private fun Label.combine(other: Label, labelSeparator: String = "_", descriptionSeparator: String = " - "): Label {
    return Label(
        label = "${label}$labelSeparator${other.label}",
        description = "${description}$descriptionSeparator${other.description}",
    )
}

private operator fun List<Label>.times(other: List<Label>): List<Label> {
    val map = this.map { label -> other.map { otherLabel -> label.combine(otherLabel) } }
    return map.flatten()
}


private fun parseBody() {

}