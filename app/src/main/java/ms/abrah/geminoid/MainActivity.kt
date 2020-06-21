package ms.abrah.geminoid

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
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
        webview.webViewClient = GeminiWebViewClient(this)


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

class GeminiWebViewClient(activityContext: Context) : WebViewClient() {
    val activityContext = activityContext

    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        Toast.makeText(activityContext, description , Toast.LENGTH_LONG).show()
        super.onReceivedError(view, errorCode, description, failingUrl)
    }

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
            val response = job.await()
            Log.d("debug", "Here's the data from the join: ${response}")

            if (response.error) {
                Log.d("error","The error we recieved was: ${response.errorDescription}")
                onReceivedError(view, ERROR_UNKNOWN, response.errorDescription, null)
                // generate toast
                return@runBlocking
            }

            response.body!!
            response.mimeType!!

            if (response.mimeType.startsWith("text/gemini")) {
                view?.loadData(
                    base64Encode(geminiContentToHtml(response.body)),
                    "text/html",
                    "base64"
                )
                return@runBlocking
            }

            view?.loadData(
                base64Encode(response.body),
                response.mimeType,
                "base64"
            )
        }

        return true
    }
}
