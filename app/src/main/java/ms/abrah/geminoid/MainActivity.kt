package ms.abrah.geminoid

import android.net.Uri
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
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val webview = this.findViewById<WebView>(R.id.webView)
        webview.webViewClient = GeminiWebViewClient()

        webview.loadData(
            textToEncodedHtml(
                "<html><body>" +
                        "'%23' is the <b>percent</b> <a href=\"gemini://sunshinegardens.org/~abrahms/\">code</a> for ‘#‘ "+
                        "</body></html>"
            ),
            "text/html",
            "base64")
    }
}

fun textToEncodedHtml(html: String): String {
    return Base64.encodeToString(html.toByteArray(), Base64.NO_PADDING)
}

suspend fun loadUrl(url: Uri?): String {
    Log.d("debug", "In the load url function")
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
    val request = "${url}\r\n"
    Log.d("debug", "request: ${request}")
    output.write(request.toByteArray())
    output.flush()
    Log.d("debug", "waiting on input")


    val writer = StringWriter()
    IOUtils.copy(input, writer, "utf-8")
    return writer.toString()
}
class GeminiLoader : AsyncTask<ContextualUrl, Void, String>() {
    public fun loadUrl(urlContext: ContextualUrl?): String {
        Log.d("debug", "In the load url function")
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

        output.write((urlContext?.url + "\r\n").toByteArray())
        output.flush()
        Log.d("debug", "waiting on input")


        val writer = StringWriter()
        IOUtils.copy(input, writer, "utf-8")
        Log.d("Browser", "Url is going to be ${urlContext?.url}")
        urlContext?.view?.loadData(
            writer.toString(),
            "text/plain",
            "base64"
        )

        Log.d("gemini output", "client received: ${writer.toString()}")
        return "Worked";
    }

    public override fun doInBackground(vararg ctx: ContextualUrl?): String {
        return loadUrl(ctx.first())
    }
}

data class ContextualUrl(val view: WebView?, val url: String?)

class GeminiWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val job = GlobalScope.async {
            loadUrl(request?.url)
        }
        runBlocking {
            val data = job.await()
            Log.d("debug", "Here's the data from the join: ${data}")
            view?.loadData(
                textToEncodedHtml(data.toString()),
                "text/plain",
                "base64"
            )
        }

        return true
    }
}
