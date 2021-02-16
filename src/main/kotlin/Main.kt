import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import table.Label
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

    val title = elements.first().select("title")
    //println(extractTitle(elements))
    println(extractLabels(elements))
}

private fun extractTitle(elements: Elements) =
    elements.first().getElementsByTag("b").first().text()

private fun extractLabels(elements: Elements) {
    val secondRow = elements[1]
    val columnNamesPartOne = extractColumnNames(secondRow)
    println(columnNamesPartOne)
    val thirdRow = elements[2]
    val columnNamesPartTwo = extractColumnNames(thirdRow).toSet()
    println(columnNamesPartTwo)

    val firstRowLabels = columnNamesPartOne.map { transformTextToLabel(it) }
    println(firstRowLabels)

    val secondRowLabels = columnNamesPartTwo.map { transformTextToLabel(it) }
    println(secondRowLabels)

    val combinedLabel = firstRowLabels[2].combine(secondRowLabels[0])
    println(combinedLabel)

    val rankLabel = firstRowLabels[0]
    println(rankLabel)

    val countryLabel = firstRowLabels[1]
    println(countryLabel)

    val lowerAndUpperHouseLabels = firstRowLabels.subList(2, 4)

    println(lowerAndUpperHouseLabels)
    println(listOf(rankLabel, countryLabel))
    println("#####")
    println(lowerAndUpperHouseLabels * secondRowLabels)
}

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