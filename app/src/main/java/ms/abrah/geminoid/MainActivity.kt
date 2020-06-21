package ms.abrah.geminoid

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import ms.abrah.geminoid.adapter.geminiContentToHtml
import ms.abrah.geminoid.client.loadUrl

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val webview = this.findViewById<WebView>(R.id.webView)
        webview.webViewClient = GeminiWebViewClient()


        webview.loadData(
            base64Encode(
                "<html><body>" +
                        "'%23' is the <b>percent</b> <a href=\"gemini://sunshinegardens.org/~abrahms/\">code</a> for ‘#‘ "+
                        "</body></html>"
            ),
            "text/html",
            "base64")
    }

    override fun onBackPressed() {
        if (this.webView.canGoBack()) {
            this.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

fun base64Encode(s: String): String {
    return Base64.encodeToString(s.toByteArray(), Base64.NO_PADDING)
}

class GeminiWebViewClient : WebViewClient() {

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        // Let the webview read the actual http stuff. This is pretty nice, actually.
        if (request?.url!!.scheme!!.startsWith("http")) {
            return false
        }

        val job = CoroutineScope(Dispatchers.IO).async {
            loadUrl(request.url)
        }
        runBlocking {
            val data = job.await()
            Log.d("debug", "Here's the data from the join: ${data}")
            view?.loadData(
                base64Encode(geminiContentToHtml(data)),
                "text/html",
                "base64"
            )
        }

        return true
    }
}
