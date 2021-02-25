package tableextractor

import extractDateFromClassifUrl
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import table.Header
import table.Label
import table.Row
import table.Table
import java.util.*

class WithTitleTableExtractor(private val document: Document): TableExtractor {

    override fun getDocument(): Document = document

    override fun extract(): Table {
        val table = super.extract()
        // fill and index table
        return fillArchiveTableWithExtrasAndIndex(table, getDocument().location())
    }

    override fun extractTableRows(doc: Document): Elements {
        val tableRows = getTableCssQuery(doc)
        if (tableRows.isEmpty()) {
            throw WithTitleTableParsingException("That table has invalid format.")
        }
        return tableRows
    }

    override fun getTableCssQuery(doc: Document): Elements {
        return doc.getTableByCssQuery("table[class=data]")
    }

    private fun Document.getTableByCssQuery(cssQuery: String): Elements = select(cssQuery).select("tbody").select("tr")
//    private fun Document.getTableCssQuery() = getTableByCssQuery("table[class=data]")
//    private fun Document.getTableCssQueryLegacy() = getTableByCssQuery("table[border=1]")

    override fun extractTitle(elements: Elements): String =
        elements.first().getElementsByTag("b").first().text()

    override fun extractHeader(elements: Elements): Header {
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

    private fun Elements.extractFirstRowLabels(): List<Label> = extractLabelsFromRow(this[1])
    private fun Elements.extractSecondRowLabels(): List<Label> = extractLabelsFromRow(this[2])

    private fun extractLabelsFromRow(row: Element): List<Label> {
        val columnNames = extractColumnNames(row)
        return columnNames.map { transformTextToLabel(it) }
    }

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

    private operator fun List<Label>.times(other: List<Label>): List<Label> {
        val map = this.map { label -> other.map { otherLabel -> label.combine(otherLabel) } }
        return map.flatten()
    }

    private fun Label.combine(other: Label, labelSeparator: String = "_", descriptionSeparator: String = " - "): Label {
        return Label(
            label = "${label}$labelSeparator${other.label}",
            description = "${description}$descriptionSeparator${other.description}",
        )
    }

    override fun extractTableData(elements: Elements): List<Row> {
        return elements.asSequence()
            .filterIndexed { index, _ -> index > 2 }
            .map { element -> tableRowToRow(element) }.toList()
    }

    private fun tableRowToRow(tableRow: Element): Row {
        val tableData = tableRow.select("td")
        val columnValues = tableData.map { element -> element.text() }
        return Row(columnValues)
    }

    fun fillArchiveTableWithExtrasAndIndex(table: Table, url: String): Table {
        // extracting extras
        val doc: Document = Jsoup.connect(url).get()
        println("Page Location:${doc.location()}")
        val situationAsOfExtra = extractSituationAsOfExtra(doc)
        val situationAsOfDateExtra = createSituationAsOfDateExtra(extractDateFromClassifUrl(url))
        val extras: Map<String, String> = mapOf(situationAsOfExtra, situationAsOfDateExtra)

        // filled and indexed table
        return table.fillWithExtras(extras).index()
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

    private fun createSituationAsOfDateExtra(date: Date) = "situation_as_of_date" to DateUtils.toSimpleString(date)

    private fun extractSituationAsOfExtra(doc: Document): Pair<String, String> {
        val situationAsOfClass = "h1852"
        val situationAsOfDate = doc.select("b[class=$situationAsOfClass]").first().text()
        return "situation_as_of" to situationAsOfDate
    }
}