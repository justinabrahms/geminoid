package ms.abrah.geminoid.adapter

import kotlin.math.min

val HEADING_LEVEL_REGEX = Regex("^(#+)")

// This assumes the response's first line isn't passed in.
fun geminiContentToHtml(content: String): String {
    val sb = StringBuilder()
    sb.append("<html><body>")
    var hasStartedUl = false
    var hasStartedP = false
    var hasStartedQuote = false
    var hasStartedPre = false

    content.lines().forEach {
        if (isLink(it)) {
            hasStartedQuote = endQuoteIfNeeded(hasStartedQuote, sb)
            hasStartedUl = endUlIfNeeded(hasStartedUl, sb)
            hasStartedP = endParagraphIfNeeded(hasStartedP, sb)
            val result = it.split(' ')
            val url = result.subList(1, 2).first()
            val text: String;
            if (result.size == 2) {
                text = url
            } else {
                text = result.subList(2, result.size).joinToString(" ")
            }
            sb.append("<p><a href=\"${url}\">${text}</a></p>")
            return@forEach
        }

        if (isBlockQuote(it)) {
            hasStartedP = endParagraphIfNeeded(hasStartedP, sb)
            hasStartedUl = endUlIfNeeded(hasStartedUl, sb)
            if (hasStartedQuote) {
                sb.append("\n")
            } else {
                hasStartedQuote = true
                sb.append("<blockquote>")
            }
            sb.append(it.trimStart('>').trim())
            return@forEach
        }

        if (isListItem(it)) {
            hasStartedQuote = endQuoteIfNeeded(hasStartedQuote, sb)
            if (!hasStartedUl) {
                hasStartedUl = true
                sb.append("<ul>")
            }
            sb.append("<li>${it.substring(2)}</li>")
            return@forEach
        }

        if (isParagraphText(it)) {
            hasStartedQuote = endQuoteIfNeeded(hasStartedQuote, sb)
            hasStartedUl = endUlIfNeeded(hasStartedUl, sb)
            if (!hasStartedP) {
                hasStartedP = true
                sb.append("<p>")
                sb.append(it)
            } else {
                // If we're continuing a paragraph, join them with a space
                sb.append(" ${it}")
            }
            return@forEach
        }

        if (isHeading(it)) {
            hasStartedQuote = endQuoteIfNeeded(hasStartedQuote, sb)
            hasStartedUl = endUlIfNeeded(hasStartedUl, sb)
            hasStartedP = endParagraphIfNeeded(hasStartedP, sb)
            val matches = HEADING_LEVEL_REGEX.find(it)
            val headingLevel = min(6, matches!!.groupValues.first().length)
            sb.append("<h${headingLevel}>${it.trimStart('#').trim()}</h${headingLevel}>")
            return@forEach
        }
    }
    hasStartedQuote = endQuoteIfNeeded(hasStartedQuote, sb)
    hasStartedUl = endUlIfNeeded(hasStartedUl, sb)
    hasStartedP = endParagraphIfNeeded(hasStartedP, sb)
    sb.append("</body></html>")
    return sb.toString()
}

fun endParagraphIfNeeded(hasStartedP: Boolean, sb: StringBuilder): Boolean {
    if (hasStartedP) {
        sb.append("</p>")
    }
    return false
}

fun endUlIfNeeded(hasStartedUl: Boolean, sb: StringBuilder): Boolean {
    if (hasStartedUl) sb.append("</ul>")
    return false
}

fun endQuoteIfNeeded(hasStartedQuote: Boolean, sb: StringBuilder): Boolean {
    if (hasStartedQuote) sb.append("</blockquote>")
    return false
}

fun isBlockQuote(it: String): Boolean {
    return it.length != 0 && it.startsWith(">")
}

fun isListItem(it: String): Boolean {
    return it.length != 0 && it.startsWith("* ")
}

fun isLink(it: String): Boolean {
    return it.length != 0 && it.startsWith("=>")
}

fun isParagraphText(it: String): Boolean {
    return it.length != 0 && !it.startsWith("#")
}

fun isHeading(it: String): Boolean {
    return it.length != 0 && it.startsWith("#")
}
