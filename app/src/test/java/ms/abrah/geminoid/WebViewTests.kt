package ms.abrah.geminoid

import android.net.Uri
import android.util.Base64
import android.webkit.WebResourceRequest
import android.webkit.WebView
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import ms.abrah.geminoid.client.loadUrl
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class WebViewTests {
    @Test
    fun webview_client_callback_writes_to_view() {
        val mockView = mockk<WebView>(relaxed=true)
        val mockRequest = mockk<WebResourceRequest>()
        val mockUri = mockk<Uri>()
        mockkStatic("android.util.Base64")
        every { Base64.encodeToString("<h1>works</h1>".toByteArray(), Base64.NO_PADDING) } returns "PGgxPndvcmtzPC9oMT4="
        mockkStatic("ms.abrah.geminoid.client.GeminiClientKt")
        // @@@ Fix this by downloading the coroutine mocking library and figuring out how that works
        // https://mockk.io/#coroutines for more info

        coEvery { loadUrl(mockUri) } returns "<h1>works</h1>"
        every { mockUri.toString() } returns "gemini://example.org"
        every { mockRequest.getUrl() } returns mockUri

        val shouldOverride =
            GeminiWebViewClient().shouldOverrideUrlLoading(mockView, mockRequest)

        assertEquals(shouldOverride, true)
    }
}

