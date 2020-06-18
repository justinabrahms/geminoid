package ms.abrah.geminoid

import android.os.AsyncTask
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import org.apache.commons.io.IOUtils
import java.io.*
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

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

class GeminiLoader : AsyncTask<String, Void, String>() {
    public fun loadUrl(url: String?): String? {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate>? = null
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        })

        val ctx = SSLContext.getInstance("SSL")
        ctx.init(null, trustAllCerts, SecureRandom())
        val socket = ctx.socketFactory.createSocket("sunshinegardens.org", 1965) as SSLSocket
        socket.enabledProtocols = arrayOf("TLSv1.3")
        socket.enabledCipherSuites = arrayOf("TLS_AES_128_GCM_SHA256")

        val input: InputStream = BufferedInputStream(socket.inputStream)
        val output: OutputStream = BufferedOutputStream(socket.outputStream)
        output.write((url + "\r\n").toByteArray())
        output.flush()

        val writer = StringWriter()
        IOUtils.copy(input, writer, "utf-8")

        Log.d("gemini output",
            String.format("client received: %s", writer.toString())
        )
        return "Worked"
    }

    public override fun doInBackground(vararg url: String?): String? {
        return loadUrl(url.first())
    }
}

class GeminiWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        GeminiLoader().execute("gemini://sunshinegardens.org/~abrahms/")

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
