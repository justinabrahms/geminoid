package ms.abrah.geminoid.adapter

// This assumes the response's first line isn't passed in.
fun geminiContentToHtml(content: String): String {
    val sb = StringBuilder()
    sb.append("<html><body>")
    var hasStartedUl = false
    var hasStartedP = false

    content.lines().forEach {
        if (isLink(it)) {
            hasStartedUl = endUlIfNeeded(hasStartedUl, sb)
            hasStartedP = endParagraphIfNeeded(hasStartedP, sb)
            val result = it.split(' ')
            val url = result.subList(1, 2).first()
            var text: String;
            if (result.size == 2) {
                text = url
            } else {
                text = result.subList(2, result.size).joinToString(" ")
            }
            sb.append("<p><a href=\"${url}\">${text}</a></p>")
            return@forEach
        }

        if (isListItem(it)) {
            if (!hasStartedUl) {
                hasStartedUl = true
                sb.append("<ul>")
            }
            sb.append("<li>${it.substring(2)}</li>")
            return@forEach
        }

        if (isParagraphText(it)) {
            hasStartedUl = endUlIfNeeded(hasStartedUl, sb)
            if (!hasStartedP) {
                hasStartedP = true
                sb.append("<p>")
                sb.append(it)
            } else {
                // If we're continuing a paragraph, join them with a space
                sb.append(" ${it}")
            }

        }
        if (isHeading(it)) {
            hasStartedUl = endUlIfNeeded(hasStartedUl, sb)
            hasStartedP = endParagraphIfNeeded(hasStartedP, sb)
            sb.append("<h1>${it.trimStart('#').trim()}</h1>")
        }
    }
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
