import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import table.Header
import table.Label
import table.Row
import table.Table
import java.io.File
import java.io.FileNotFoundException
import java.util.*


fun main() {
    val baseUrl = "http://archive.ipu.org"
    val url = "$baseUrl/wmn-e/classif-arc.htm"
    val pageReader: PageReader = HtmlPageReader(url)
    val pageContent = pageReader.read()
    val hrefs = HrefExtractor(url, pageContent).extract()
    val archiveHrefs = hrefs.filter { href -> href.path.contains("/arc/", ignoreCase = true) }
    val archivesUrls = archiveHrefs.map { it.toArchiveUrl() }

    try {
        val archiveContent = HtmlPageReader("$baseUrl${archivesUrls[10].path}").read()
        //println(archiveContent.content)
        extractDataFromDoc("$baseUrl${archivesUrls[10].path}")
    } catch (exc: FileNotFoundException) {
        println("file not found exception")
    }

}

fun extractDataFromDoc(url: String) {

    val doc: Document
    try {
        doc = Jsoup.connect(url).get()
    } catch (exc: Exception) {
        return
    }

    val situationAsOfExtra = extractSituationAsOfExtra(doc)
    val situationAsOfDateExtra = createSituationAsOfDateExtra(extractDateFromClassifUrl(url))
    val extras: Map<String, String> = mapOf(situationAsOfExtra, situationAsOfDateExtra)

    val table: Table = createTableFromDoc(doc)
    val filledTable = table.fillWithExtras(extras)
    val indexedTable = filledTable.index()

    println(indexedTable)
    CsvTableWriter(file = File("./content.csv")).write(indexedTable)
}

private fun Table.index(): Table {
    val indexLabel =  "idx"
    val labelsWithIndex = listOf(Label(indexLabel, indexLabel)) + this.header.labels
    val indexedHeader = Header(labelsWithIndex)
    val indexedRows = this.rows.mapIndexed { index, row -> Row(listOf("${index+1}") + row.value) }
    return Table(this.title, indexedHeader, indexedRows)
}

private fun Table.fillWithExtras(extras: Map<String, String>): Table {
    val title = this.title
    val filledHeader = header.addLabelsLeft(extras.keys.toList())
    val filledRows = rows.addColumnsLeft(extras.values.toList())
    return Table(title, filledHeader, filledRows)
}

private fun Header.addLabelsLeft(otherLabelsAsString: List<String>): Header {
    val otherLabels = otherLabelsAsString.map { Label(it, it) }
    return Header(otherLabels + labels)
}
private fun List<Row>.addColumnsLeft(columns: List<String>): List<Row> = map { row -> Row(columns + row.value) }

private fun createTableFromDoc(doc: Document): Table {
    val tableRows = extractTableRows(doc)
    with(tableRows) {
        val title = extractTitle(this)
        val header = extractHeader(this)
        val tableData = extractTableData(this)
        return Table(title, header, tableData)
    }
}


private fun createSituationAsOfDateExtra(date: Date) = "situation_as_of_date" to DateUtils.toSimpleString(date)

private fun extractSituationAsOfExtra(doc: Document): Pair<String, String> {
    val situationAsOfClass = "h1852"
    val situationAsOfDate = doc.select("b[class=$situationAsOfClass]").first().text()
    return "situation_as_of" to situationAsOfDate
}

private fun extractTableRows(doc: Document): Elements =
    doc.select("table[class=data]").select("tbody").select("tr")

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

/**
 * Removing duplicates with toSet.
 */
private fun extractColumnNames(element: Element) =
    element.select("td").map { it.getElementsByTag("b").first().text() }.toSet().toList()

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