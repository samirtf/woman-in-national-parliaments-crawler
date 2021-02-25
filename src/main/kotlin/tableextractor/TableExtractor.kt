package tableextractor

import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import table.Header
import table.Row
import table.Table

open interface TableExtractor {

    fun getDocument(): Document

    fun extract(): Table {
        val tableRows = extractTableRows(getDocument())
        with(tableRows) {
            val title = extractTitle(this)
            val header = extractHeader(this)
            val tableData = extractTableData(this)
            return Table(title, header, tableData)
        }
    }

    open fun extractTableRows(doc: Document): Elements

    open fun getTableCssQuery(doc: Document): Elements

    fun extractTitle(elements: Elements): String

    fun extractHeader(elements: Elements): Header

    fun extractTableData(elements: Elements): List<Row>

}