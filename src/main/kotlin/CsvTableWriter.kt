import table.Table
import java.io.File
import java.lang.Exception
import java.io.PrintWriter

import java.io.BufferedWriter

import java.io.FileWriter




class CsvTableWriter(private val file: File, private val separator: String = ","): TableWriter {

    override fun write(table: Table) {
        var printWriter: PrintWriter? = null
        try {
            if (!file.exists()) { file.createNewFile() }
            val fileWriter = FileWriter(file, true)
            val bufferedWriter = BufferedWriter(fileWriter)
            printWriter = PrintWriter(bufferedWriter)
            val formatter: TableFormatter = CsvTableFormatter(table, separator = separator)
            printWriter.write(formatter.getTitle())
            printWriter.write(formatter.getHeader())
            printWriter.write(formatter.getRows())
        } catch (exc: Exception) {
            exc.printStackTrace()
        } finally {
            printWriter?.close()
        }
    }

    private fun PrintWriter.write(list: List<String>) = list.forEach { write(it) }

}