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
    fun content_textSingleHeading6() {
        val c = "###### this is text"
        assertEquals(geminiContentToHtml(c), preambleWrap("<h6>this is text</h6>"))
    }

    @Test
    fun content_textSingleHeading6_evenIf7() {
        val c = "####### this is text"
        assertEquals(geminiContentToHtml(c), preambleWrap("<h6>this is text</h6>"))
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
    fun content_linkWithDescription() {
        val c = "=> works with text"
        assertEquals(geminiContentToHtml(c), preambleWrap("<p><a href=\"works\">with text</a></p>"))
    }

    @Test
    fun content_linkBare() {
        val c = "=> works"
        assertEquals(geminiContentToHtml(c), preambleWrap("<p><a href=\"works\">works</a></p>"))
    }

    @Test
    fun content_list() {
        val c = "* works"
        assertEquals(geminiContentToHtml(c), preambleWrap("<ul><li>works</li></ul>"))
    }

    @Test
    fun content_multiList() {
        val c = "* works\n* too"
        assertEquals(geminiContentToHtml(c), preambleWrap("<ul><li>works</li><li>too</li></ul>"))
    }

    @Test
    fun content_quoteSimple() {
        val c = "> works"
        assertEquals(geminiContentToHtml(c), preambleWrap("<blockquote>works</blockquote>"))
    }

    @Test
    fun content_quoteComplex() {
        val c = "text\n> works\n>test\nfoo"
        assertEquals(geminiContentToHtml(c), preambleWrap("<p>text</p><blockquote>works\ntest</blockquote><p>foo</p>"))
    }

    @Test
    fun content_pre() {
        val c = "```\ntest\nfoo\n```"
        assertEquals(preambleWrap("<pre>test\nfoo</pre>"), geminiContentToHtml(c))
    }

    @Test
    fun content_preWithMarkupInIt() {
        val c= "```\n* test\n> foo\nbar\n```"
        assertEquals(preambleWrap("<pre>* test\n> foo\nbar</pre>"), geminiContentToHtml(c))
    }

    @Test
    fun content_twoPreTags() {
        val c = "```\nfoo\n```\n```\nbar\n```"
        assertEquals(preambleWrap("<pre>foo</pre><pre>bar</pre>"), geminiContentToHtml(c))
    }
}
