package ms.abrah.geminoid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val webview = this.findViewById<WebView>(R.id.webView)
        webview.webViewClient = GeminiWebViewClient()

        webview.loadData(
            textToEncodedHtml(
                "<html><body>" +
                        "'%23' is the <b>percent</b> <a href=\"gemini://test\">code</a> for ‘#‘ "+
                        "</body></html>"
            ),
            "text/html",
            "base64")
    }
}

fun textToEncodedHtml(html: String): String {
    return Base64.encodeToString(html.toByteArray(), Base64.NO_PADDING)
}

class GeminiWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        // In here, we'd actually interpret the link, convert it to gemini url
        Log.d("Browser", "Url is going to be " + request?.url.toString())
        view?.loadData(textToEncodedHtml(
            "<html><body>Wo<b>rk</b>s!</body></html>"),
            "text/html",
            "base64"
        )
        return true
    }
}
