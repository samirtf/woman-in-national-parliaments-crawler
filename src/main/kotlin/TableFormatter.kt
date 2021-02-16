interface TableFormatter {
    fun getTitle(): String
    fun getHeader(): String
    fun getRows(): List<String>
}