package ms.abrah.geminoid.client

import android.net.Uri
import android.util.Log
import org.apache.commons.io.IOUtils
import java.io.*
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

fun loadUrl(url: Uri?): String {
    Log.d("debug", "In the load url function")
    val trustAllCerts = arrayOf<TrustManager>(object :
        X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate>? = null
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
    })

    // TODO(abrahmsj): Move this to non-blocking IO
    // https://examples.javacodegeeks.com/core-java/nio/java-nio-ssl-example/ as an example
    val ctx = SSLContext.getInstance("SSL")
    ctx.init(null, trustAllCerts, SecureRandom())
    val socket = ctx.socketFactory.createSocket("sunshinegardens.org", 1965) as SSLSocket
    socket.enabledProtocols = arrayOf("TLSv1.3")
    socket.enabledCipherSuites = arrayOf("TLS_AES_128_GCM_SHA256")

    val input: InputStream =
        BufferedInputStream(socket.inputStream)
    val output: OutputStream =
        BufferedOutputStream(socket.outputStream)
    val request = "${url}\r\n"
    Log.d("debug", "request: ${request}")
    output.write(request.toByteArray())
    output.flush()
    Log.d("debug", "waiting on input")


    val writer = StringWriter()
    IOUtils.copy(input, writer, "utf-8")
    socket.close()
    val response = writer.toString()
    val responseLines = response.split('\n')
    return responseLines.subList(1, responseLines.size).joinToString("\n")
}