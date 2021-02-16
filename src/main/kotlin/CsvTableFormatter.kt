import table.Row
import table.Table

class CsvTableFormatter(
    private val table: Table,
    private val omitTitle: Boolean = true,
    private val separator: String = ","
): TableFormatter {

    override fun getTitle(): String = if (omitTitle) "" else transformTitle(table.title)

    private fun transformTitle(title: String) =
        title.split(" ").joinToString { s -> s.capitalize() }.addLineBreak()

    override fun getHeader() =
        table.header.labels.joinToString(separator = separator) { label -> label.label }.addLineBreak()

    override fun getRows(): List<String> = table.rows.map { row -> transformRow(row) }

    private fun transformRow(row: Row) = row.value.joinToString(separator).addLineBreak()

    private fun String.addLineBreak() = "${this}\n"
}