package ms.abrah.geminoid.adapter

import org.junit.Assert.assertEquals
import org.junit.Test

class GeminiContentAdapterTest {
    fun preambleWrap(s:String): String {
        return "<html><body>${s}</body></html>"
    }

    @Test
    fun content_empty() {
        assertEquals(geminiContentToHtml(""), "<html><body></body></html>")
    }

    @Test
    fun content_textSingleLine() {
        val c = "this is text"
        assertEquals(geminiContentToHtml(c), preambleWrap("<p>this is text</p>"))
    }

    @Test
    fun content_textSingleHeading() {
        val c = "# this is text"
        assertEquals(geminiContentToHtml(c), preambleWrap("<h1>this is text</h1>"))
    }

    @Test
    fun content_headingAndText() {
        val c = "# heading\nand then some text"
        assertEquals(geminiContentToHtml(c), preambleWrap("<h1>heading</h1><p>and then some text</p>"))
    }

    @Test
    fun content_textAndHeading() {
        val c = "and then some text\n# heading"
        assertEquals(geminiContentToHtml(c), preambleWrap("<p>and then some text</p><h1>heading</h1>"))
    }

    @Test
    fun content_multilineParagraph() {
        val c = "test\nit"
        assertEquals(geminiContentToHtml(c), preambleWrap("<p>test it</p>"))
    }

    @Test
    fun content_link() {
        val c = "=> works with text"
        assertEquals(geminiContentToHtml(c), preambleWrap("<p><a href=\"works\">with text</a></p>"))
    }

    @Test
    fun content_list() {
        val c = "* works"
        assertEquals(geminiContentToHtml(c), preambleWrap("<ul><li>works</li></ul>"))

    }

    @Test
    fun content_multiList() {

    }
}
