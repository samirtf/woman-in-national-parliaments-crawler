import java.util.regex.Pattern

fun extractByRegex(text:String, regex: String): List<String> {
        val matches = mutableListOf<String>()
        val pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            matches.add(text.substring(matcher.start(0), matcher.end(0)))
        }
        return matches
    }
