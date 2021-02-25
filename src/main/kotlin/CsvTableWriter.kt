import table.Table
import java.io.File
import java.lang.Exception
import java.io.PrintWriter

import java.io.BufferedWriter

import java.io.FileWriter




class CsvTableWriter(
    private val file: File,
    private val separator: String = ",",
    private val includeTitle: Boolean = true,
    private val includeHeader: Boolean = true,
    private val includeRows: Boolean = true,
): TableWriter {

    override fun write(table: Table) {
        var printWriter: PrintWriter? = null
        try {
            if (!file.exists()) { file.createNewFile() }
            val fileWriter = FileWriter(file, true)
            val bufferedWriter = BufferedWriter(fileWriter)
            printWriter = PrintWriter(bufferedWriter)
            with(printWriter) {
                val formatter: TableFormatter = CsvTableFormatter(table, separator = separator)
                writeTitleIfNecessary(formatter)
                writeHeaderIfNecessary(formatter)
                writeRowsIfNecessary(formatter)
            }
        } catch (exc: Exception) {
            exc.printStackTrace()
        } finally {
            printWriter?.close()
        }
    }

    private fun PrintWriter.writeTitleIfNecessary(formatter: TableFormatter) {
        if (includeTitle) write(formatter.getTitle())
    }

    private fun PrintWriter.writeHeaderIfNecessary(formatter: TableFormatter) {
        if (includeHeader) write(formatter.getHeader())
    }

    private fun PrintWriter.writeRowsIfNecessary(formatter: TableFormatter) {
        if (includeRows) write(formatter.getRows())
    }

    private fun PrintWriter.write(list: List<String>) = list.forEach { write(it) }

}